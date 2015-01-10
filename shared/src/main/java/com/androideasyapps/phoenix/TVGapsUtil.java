package com.androideasyapps.phoenix;

import com.androideasyapps.phoenix.dao.H2PersistenceManager;
import com.androideasyapps.phoenix.dao.MediaFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by seans on 04/01/15.
 */
public class TVGapsUtil {
    public static class Pair<T> {
        public Pair(T f, T s) {
            this.first=f;
            this.second=s;
        }
        public T first;
        public T second;
    }

    public static Collection<MediaFile> getTVGapFiles(Future<H2PersistenceManager> dao) throws Exception {
        Collection<MediaFile> files = new ArrayList<>();
        Map<String,List<Pair<Integer>>> gaps = getTVGaps(dao);
        for (Map.Entry<String, List<Pair<Integer>>> me: gaps.entrySet()) {
            for (Pair<Integer> p: me.getValue()) {
                MediaFile mf = new MediaFile();
                mf.setTitle(me.getKey());
                mf.setEpisodeTitle("N/A");
                mf.setSeason(p.first);
                mf.setEpisode(p.second);
                mf.setOriginalAirDate(System.currentTimeMillis());
                mf.setMediaType("tv");

                files.add(mf);
            }
        }
        return files;
    }

    public static Observable<Collection<MediaFile>> getTVGapFilesObserable(final Future<H2PersistenceManager> dao) {
        return Observable.create(new Observable.OnSubscribe<Collection<MediaFile>>() {
            @Override
            public void call(Subscriber<? super Collection<MediaFile>> subscriber) {
                try {
                    subscriber.onStart();
                    subscriber.onNext(getTVGapFiles(dao));
                } catch (Throwable t) {
                    subscriber.onError(t);
                }
            }
        });
    };


    public static Map<String, List<Pair<Integer>>> getTVGaps(Future<H2PersistenceManager> dao) throws Exception {
        String allShowsQuery = "mediatype='tv' order by title, season, episode";
        Collection<MediaFile> files = dao.get().getMediaFileDAO().query(allShowsQuery);
        return getTVGaps(files);
    }

    public static Map<String, List<Pair<Integer>>> getTVGaps(Collection<MediaFile> files) throws Exception {
        Map<String, List<Pair<Integer>>> gaps = new TreeMap<>();
        String lastTitle=null;
        int lastSeason=-1;
        int lastEpisode=0;
        for (MediaFile mf: files) {
            if (!mf.getTitle().equalsIgnoreCase(lastTitle)) {
                lastTitle=mf.getTitle();
                lastSeason=0;
                lastEpisode=0;
            }

            if (mf.getSeason()!=lastSeason) {
                lastSeason=mf.getSeason();
                lastEpisode=0;
            }
            if (lastEpisode!=0) {
                fillGaps(gaps, lastTitle, lastSeason, lastEpisode, mf.getEpisode());
            }
            lastEpisode=mf.getEpisode();
        }
        return gaps;
    }

    private static void fillGaps(Map<String, List<Pair<Integer>>> gaps, String title, int lastSeason, int lastEpisode, int episode) {
        if (episode-lastEpisode>1) {
            List<Pair<Integer>> g = gaps.get(title);
            if (g==null) {
                g=new ArrayList<>();
                gaps.put(title, g);
            }
            for (int i=lastEpisode+1;i<episode;i++) {
                g.add(new Pair<Integer>(lastSeason, i));
            }
        }

    }
}
