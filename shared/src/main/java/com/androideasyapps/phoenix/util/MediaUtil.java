package com.androideasyapps.phoenix.util;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.dao.Server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.androideasyapps.phoenix.util.Util.nullIfEmpty;

/**
 * Created by seans on 21/12/14.
 */
public class MediaUtil {
    static Pattern TVSEASONPattern = Pattern.compile("S([0-9]+)[ex\\.]+([0-9]+)", Pattern.CASE_INSENSITIVE);
    static Pattern TVAiringPattern = Pattern.compile("-([0-9]{4,12})-([0-9]{1,2})\\.", Pattern.CASE_INSENSITIVE);


    public static boolean isTVSeason(String name) {
        Matcher m = TVSEASONPattern.matcher(name);
        return m.find();
    }

    public static boolean isTVAiring(String name) {
        Matcher m = TVAiringPattern.matcher(name);
        return m.find();
    }

    public static String getGenres(String[] showCategoriesList) {
        if (showCategoriesList==null || showCategoriesList.length==0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<showCategoriesList.length;i++) {
            if (i>0) sb.append(" / ");
            sb.append(showCategoriesList[i]);
        }
        return sb.toString();
    }

    public static String getFileName(com.androideasyapps.phoenix.services.sagetv.model.MediaFile mf) {
        if (mf.SegmentFiles!=null&&mf.SegmentFiles.length>0) {
            return nullIfEmpty(mf.SegmentFiles[0]);
        }
        return null;
    }

    public static int getFileCount(com.androideasyapps.phoenix.services.sagetv.model.MediaFile mf) {
        if (mf.SegmentFiles!=null) {
            return mf.SegmentFiles.length;
        }
        return 0;
    }

    public static String getBackgroundURL(Server server, MediaFile mf) {
        if (mf.getMediaFileId()>0) {
            return getBaseServerUrl(server) + "/sagex/media/background/" + mf.getMediaFileId();
        } else {
            if (mf.getSeason()>0) {
                return getBaseServerUrl(server) + "/sagex/media/fanart?title=" + URLEncode(mf.getTitle()) + "&mediatype=" + URLEncode(mf.getMediaType()) + "&artifact=background&season="+String.valueOf(mf.getSeason());
            } else {
                return getBaseServerUrl(server) + "/sagex/media/fanart?title=" + URLEncode(mf.getTitle()) + "&mediatype=" + URLEncode(mf.getMediaType()) + "&artifact=background";
            }
        }
    }

    private static String URLEncode(String str) {
        if (str==null) return "";
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return URLEncoder.encode(str);
        }
    }

    public static String getPosterURL(Server server, MediaFile mf) {
        if (mf.getMediaFileId()>0) {
            return getBaseServerUrl(server) + "/sagex/media/poster/" + mf.getMediaFileId();
        } else {
            if (mf.getSeason()>0) {
                return getBaseServerUrl(server) + "/sagex/media/fanart?title=" + URLEncode(mf.getTitle()) + "&mediatype=" + URLEncode(mf.getMediaType()) + "&artifact=poster&season="+String.valueOf(mf.getSeason());
            } else {
                return getBaseServerUrl(server) + "/sagex/media/fanart?title=" + URLEncode(mf.getTitle()) + "&mediatype=" + URLEncode(mf.getMediaType()) + "&artifact=poster";
            }
        }
    }

    public static String getBaseServerUrl(Server server) {
        return "http://" + server.getHost() + ":" + server.getPort();
    }

    public static String getBaseServerUrlWithEmbeddedAuth(Server server) {
        return "http://sage:frey@" + server.getHost() + ":" + server.getPort();
    }

    public static String getVideoURL(Server server, MediaFile movie) {
        // TODO: Need to store # of segments and provide a getVideoUrl(movie, segment)
        return getBaseServerUrl(server) + "/sagex/media/mediafile/" + movie.getMediaFileId() + "?force-mime=video/mp4";
    }

    public static String getVideoURLWithEmbeddedAuth(Server server, MediaFile movie) {
        // TODO: Need to store # of segments and provide a getVideoUrl(movie, segment)
        return getBaseServerUrlWithEmbeddedAuth(server) + "/sagex/media/mediafile/" + movie.getMediaFileId() + "?force-mime=video/mp4";
    }

    public static String formatShortDateTimeForAiring(MediaFile file) {
        if (isToday(file.getOriginalAirDate())) {
            return formatTime(file.getOriginalAirDate()) + " Today" ;
        } else if (isTomorrow(file.getOriginalAirDate())) {
            return formatTime(file.getOriginalAirDate()) + " Tomorrow";
        } else {
            SimpleDateFormat df = new SimpleDateFormat("h:mm a - EEE, MMM d");
            return df.format(new Date(file.getOriginalAirDate()));
        }
    }

    public static String formatTime(long time) {
        SimpleDateFormat df = new SimpleDateFormat("h:mm a");
        return df.format(new Date(time));
    }

    public static boolean isTomorrow(long time) {
        return dateDayDiff(time)==1;
    }

    public static int dateDayDiff(long time) {
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.setTimeInMillis(time);
        return then.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(long time) {
        return dateDayDiff(time)==0;
    }

    public static boolean isGroupedItem(MediaFile item) {
        return item.getUserdata()!=null&&item.getUserdata().trim().length()>0;
    }

    public static boolean isTV(MediaFile movie) {
        return movie!=null && "tv".equalsIgnoreCase(movie.getMediaType());
    }

    public static CharSequence getMediaDescription(MediaFile file) {
        if (isTV(file)) {
            return getTVContextText(file);
        } else {
            return getMovieContentText(file);
        }
    }

    public static CharSequence getTVContextText(MediaFile file) {
        if (file.getEpisode()>0) {
            return String.format("%dx%d %s",file.getSeason(), file.getEpisode(), file.getEpisodeTitle());
        } else {
            return file.getEpisodeTitle();
        }
    }

    public static CharSequence getMovieContentText(MediaFile movie) {
        long minutes = movie.getDuration() / (60 * 1000);
        String str = String.format("%d mins", minutes);
        return str + ((movie.getGenre()!=null)?(" " + movie.getGenre()):"");
    }

    public static boolean isMovie(MediaFile file) {
        return file!=null&&"movie".equalsIgnoreCase(file.getMediaType());
    }
}
