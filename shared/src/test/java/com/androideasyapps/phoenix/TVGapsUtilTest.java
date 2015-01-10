package com.androideasyapps.phoenix;

import com.androideasyapps.phoenix.dao.MediaFile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.androideasyapps.phoenix.TVGapsUtil.*;

import static org.junit.Assert.*;

public class TVGapsUtilTest {

    @Test
    public void testGetTVGaps() throws Exception {
        List<MediaFile> files = new ArrayList<>();
        files.add(newMediaFile("Series1", 1, 1));
        files.add(newMediaFile("Series1", 1, 2));
        files.add(newMediaFile("Series1", 1, 5));
        files.add(newMediaFile("Series1", 1, 6));
        files.add(newMediaFile("Series1", 1, 8));
        files.add(newMediaFile("Series1", 2, 2));
        files.add(newMediaFile("Series1", 2, 4));

        files.add(newMediaFile("Series2", 1, 4));
        files.add(newMediaFile("Series2", 1, 5));
        files.add(newMediaFile("Series2", 1, 7));
        files.add(newMediaFile("Series2", 2, 1));
        files.add(newMediaFile("Series2", 2, 4));
        files.add(newMediaFile("Series2", 3, 1));
        files.add(newMediaFile("Series2", 3, 3));

        Map<String, List<Pair<Integer>>> gaps = getTVGaps(files);
        dump(gaps);

        verify(gaps.get("Series1"), new Pair(1, 3), new Pair(1, 4), new Pair(1, 7), new Pair(2, 3));
        verify(gaps.get("Series2"), new Pair(1, 6), new Pair(2, 2), new Pair(2,3), new Pair(3,2));
    }

    private void verify(List<TVGapsUtil.Pair<Integer>> gaps, TVGapsUtil.Pair... expected) {
        assertEquals(gaps.size(), expected.length);
        for (int i=0;i<expected.length;i++) {
            assertEquals(expected[i].first, gaps.get(i).first);
            assertEquals(expected[i].second, gaps.get(i).second);
        }
    }

    private void dump(Map<String, List<TVGapsUtil.Pair<Integer>>> gaps) {
        for (Map.Entry<String, List<TVGapsUtil.Pair<Integer>>> me: gaps.entrySet()) {
            System.out.println("BEGIN: " + me.getKey());
            for (TVGapsUtil.Pair<Integer> p: me.getValue()) {
                System.out.printf("  %s x %s\n", p.first, p.second);
            }
            System.out.println("END: " + me.getKey() + "\n");
        }
    }

    private MediaFile newMediaFile(String series1, int season, int ep) {
        MediaFile mf = new MediaFile();
        mf.setTitle(series1);
        mf.setSeason(season);
        mf.setEpisode(ep);
        return mf;
    }
}