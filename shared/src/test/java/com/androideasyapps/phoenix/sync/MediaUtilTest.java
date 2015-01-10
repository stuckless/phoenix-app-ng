package com.androideasyapps.phoenix.sync;

import com.androideasyapps.phoenix.util.MediaUtil;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

import static com.androideasyapps.phoenix.util.MediaUtil.*;

public class MediaUtilTest {

    @Test
    public void testIsTVSeason() throws Exception {
        assertTrue(isTVSeason("/opt/MEDIA/videos/tv/NCIS- New Orleans/NCIS- New Orleans S01E08 Love Hurts.mp4"));
        assertTrue(isTVSeason("/opt/MEDIA/videos/tv/NCIS- New Orleans/NCIS- New Orleans S01x08 Love Hurts.mp4"));
        assertFalse(isTVSeason("/opt/MEDIA/videos/tv/NCIS- New Orleans/NCIS- New Orleans S0108 Love Hurts.mp4"));
    }

    @Test
    public void testIsTVAiring() throws Exception {
        assertTrue(isTVAiring("/opt/MEDIA/sagetv/tv/PersonofInterest-TheDevilYouKnow-12665609-0.ts"));
        assertTrue(isTVAiring("/opt/MEDIA/sagetv/tv/PersonofInterest-TheDevilYouKnow-12665609-3.ts"));
        assertFalse(isTVAiring("/opt/MEDIA/sagetv/tv/PersonofInterest-TheDevilYouKnow-12665609-0-22.ts"));
        assertFalse(isTVAiring("/opt/MEDIA/sagetv/tv/PersonofInterest-TheDevilYouKnow-12665609.ts"));
        assertFalse(isTVAiring("/opt/MEDIA/sagetv/tv/PersonofInterest-TheDevilYouKnow-126-2.ts"));
    }

    @Test
    public void testDateDiff() {
        Calendar time = Calendar.getInstance();
        System.out.println(MediaUtil.dateDayDiff(time.getTimeInMillis()));
        assertTrue(MediaUtil.isToday(time.getTimeInMillis()));

        time.add(Calendar.DAY_OF_YEAR, 1);
        System.out.println(MediaUtil.dateDayDiff(time.getTimeInMillis()));
        assertFalse(MediaUtil.isToday(time.getTimeInMillis()));
        assertTrue(MediaUtil.isTomorrow(time.getTimeInMillis()));
    }
}