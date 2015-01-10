package com.androideasyapps.phoenix.sync;

import com.androideasyapps.phoenix.dao.H2PersistenceManager;
import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.dao.MediaFileH2DAO;
import com.androideasyapps.phoenix.services.sagetv.SageTVService;
import com.androideasyapps.phoenix.services.sagetv.model.MediaFileResults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.androideasyapps.phoenix.util.MediaUtil.getFileCount;
import static com.androideasyapps.phoenix.util.MediaUtil.getFileName;
import static com.androideasyapps.phoenix.util.MediaUtil.getGenres;
import static com.androideasyapps.phoenix.util.MediaUtil.isTVAiring;
import static com.androideasyapps.phoenix.util.MediaUtil.isTVSeason;
import static com.androideasyapps.phoenix.util.Util.firstNonNull;
import static com.androideasyapps.phoenix.util.Util.parseInt;

/**
 * Created by seans on 20/12/14.
 */
public class SageTVSync {
    private static final Logger log = LoggerFactory.getLogger(SageTVSync.class);

    public Observable<MediaFile> syncCollection(final SageTVService service, final Future<H2PersistenceManager> persistenceManagerFuture, SyncOptions options) {
        final SyncOptions opts = (options==null?new SyncOptions():options);
        return Observable.create(new Observable.OnSubscribe<MediaFile>() {

                                     @Override
                                     public void call(Subscriber<? super MediaFile> subscriber) {
                                         final MediaFileH2DAO dao;
                                         log.info("Starting SageTV Sync");
                                         int last = 0;
                                         int size = 100;
                                         if (subscriber.isUnsubscribed()) return;
                                         subscriber.onStart();

                                         try {
                                             dao = persistenceManagerFuture.get().getMediaFileDAO();
                                             if (opts.isEraseCollection()) {
                                                 dao.deleteAll();
                                             }
                                         } catch (Exception e) {
                                             log.error("Failed to get Persistence Manager", e);
                                             subscriber.onError(e);
                                             return;
                                         }

                                         try {
                                             List<Long> sageids = new ArrayList<Long>();
                                             while (true) {
                                                 log.info("Processing {}-{} items", last, last + size);

                                                 // do this in chunks of 100 untile we have everything...
                                                 MediaFileResults results = service.getMediaFiles("TV", last, size);
                                                 if (results == null || results.getResult() == null || results.getResult().size() == 0) {
                                                     // we are done
                                                     break;
                                                 }
                                                 MediaFile existingMF=null;
                                                 MediaFile mfdao = null;
                                                 for (com.androideasyapps.phoenix.services.sagetv.model.MediaFile mf : results.getResult()) {
                                                     //System.out.println("MF: " + mf.MediaFileMetadataProperties.MediaTitle);

                                                     if (mf.Airing == null) {
                                                         log.warn("Airing is Null for: {}", mf.MediaFileID);
                                                         continue;
                                                     }
                                                     sageids.add(mf.MediaFileID);
                                                     existingMF=null;
                                                     try {
                                                         existingMF = dao.queryFirst("mediafileid=?", mf.MediaFileID);
                                                     } catch (Exception e) {
                                                         log.error("Failed which finding {}", mf.MediaFileID, e);
                                                         continue;
                                                     }

                                                     if (mf.Airing.Show == null) {
                                                         log.warn("Airing.Show is Null for: {}", mf.MediaFileID);
                                                         continue;
                                                     }

                                                     if (mf.MediaFileID == 0) {
                                                         continue;
                                                     }

                                                     if (existingMF!=null) {
                                                         mfdao = existingMF;
                                                     } else {
                                                         mfdao = new MediaFile();
                                                     }

                                                     String mediaType = mf.MediaFileMetadataProperties.MediaType;
                                                     if (mediaType == null || mediaType.trim().length()==0) {
                                                         String file = getFileName(mf);
                                                         if (file == null) {
                                                             log.warn("No Physical File, Skipping: {}", mf.MediaFileID);
                                                             continue;
                                                         } else {
                                                             // missing metadata
                                                             log.warn("MediaType was null for {} - {}; Will Auto Determine Type.", mf.MediaFileID, file);
                                                             if (isTVAiring(file) || isTVSeason(file)) {
                                                                 mfdao.setMediaType("TV");
                                                             } else {
                                                                 mfdao.setMediaType("MOVIE");
                                                             }
                                                         }
                                                         mfdao.setHasmetadata(false);
                                                     } else {
                                                         mfdao.setHasmetadata(true);
                                                     }

                                                     mfdao.setGenre(getGenres(mf.Airing.Show.ShowCategoriesList));
                                                     mfdao.setDescription(firstNonNull(mf.MediaFileMetadataProperties.Description, mf.Airing.Show.ShowDescription));
                                                     mfdao.setDuration(mf.Airing.AiringDuration);

                                                     mfdao.setEpisode(parseInt(firstNonNull(mf.MediaFileMetadataProperties.EpisodeNumber, mf.Airing.Show.ShowEpisodeNumber)));
                                                     mfdao.setSeason(parseInt(firstNonNull(mf.MediaFileMetadataProperties.SeasonNumber, mf.Airing.Show.ShowSeasonNumber)));
                                                     mfdao.setEpisodeTitle(mf.MediaFileMetadataProperties.EpisodeName);
                                                     mfdao.setFileDate(mf.FileStartTime);
                                                     mfdao.setMediaFileId(mf.MediaFileID);
                                                     mfdao.setMediaType(mf.MediaFileMetadataProperties.MediaType);
                                                     mfdao.setOriginalAirDate(mf.Airing.AiringStartTime);
                                                     mfdao.setTitle(firstNonNull(mf.MediaFileMetadataProperties.MediaTitle, mf.Airing.Show.ShowEpisode, mf.MediaFileRelativePath));
                                                     mfdao.setUserRating(parseInt(mf.MediaFileMetadataProperties.UserRating));
                                                     mfdao.setWatched(mf.Airing.IsWatched);
                                                     mfdao.setYear(parseInt(firstNonNull(mf.MediaFileMetadataProperties.Year, mf.Airing.Show.ShowYear)));
                                                     mfdao.setRating(mf.MediaFileMetadataProperties.Rated);
                                                     mfdao.setParts(getFileCount(mf));

                                                     if (mfdao.getTitle() == null) {
                                                         log.warn("Null Title for {} - {}; Skipping.", mf.MediaFileID, getFileName(mf));
                                                         continue;
                                                     }

                                                     if (mfdao.getMediaType() == null) {
                                                         mfdao.setMediaType("MOVIE");
                                                     }

                                                     mfdao.setMediaType(mfdao.getMediaType().trim().toLowerCase());

                                                     try {
                                                         dao.save(mfdao);
                                                     } catch (Exception e) {
                                                         e.printStackTrace();
                                                     }
                                                     if (!subscriber.isUnsubscribed()) {
                                                         subscriber.onNext(mfdao);
                                                     }
                                                 }

                                                 last += size;
                                             }

                                             // delete the deleted ones
                                             if (sageids.size()>0) {
                                                 StringBuilder sb = new StringBuilder("mediatype in ('tv','movie') and mediafileid not in (");
                                                 for (int i=0;i<sageids.size();i++) {
                                                     if (i>0) {
                                                         sb.append(",");
                                                     }
                                                     sb.append(String.valueOf(sageids.get(i)));
                                                 }
                                                 sb.append(")");
                                                 log.info("DELETING: " + sb.toString());
                                                 dao.deleteWhere(sb.toString());
                                             }

                                             // clear the ids
                                             sageids.clear();
                                         } catch (Throwable t) {
                                             log.error("Sync Failed", t);
                                             if (!subscriber.isUnsubscribed()) subscriber.onError(t);
                                         } finally {
                                             if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                                         }

                                     }
                                 }

        ).

                subscribeOn(Schedulers.io()

                );
    }

}
