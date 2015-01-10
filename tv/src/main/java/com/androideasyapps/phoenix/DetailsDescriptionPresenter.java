/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.androideasyapps.phoenix;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.util.MediaUtil;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        MediaFile movie = (MediaFile) item;

        if (movie != null) {
            if ("tv".equalsIgnoreCase(movie.getMediaType())) {
                if (MediaUtil.isGroupedItem(movie)) {
                    viewHolder.getTitle().setText(movie.getTitle());
                    if (movie.getParts()>1) {
                        viewHolder.getSubtitle().setText(String.valueOf(movie.getParts()) + " Episodes");
                    }
                } else {
                    viewHolder.getTitle().setText(movie.getTitle() + " - " + movie.getEpisodeTitle());
                    viewHolder.getSubtitle().setText(String.format("Season %s Episode %s", movie.getSeason(), movie.getEpisode()));
                    String desc = MediaUtil.formatShortDateTimeForAiring(movie) + "\n" + movie.getDescription();
                    viewHolder.getBody().setText(desc);
                }
            } else {
                viewHolder.getTitle().setText(movie.getTitle());
                viewHolder.getSubtitle().setText(movie.getGenre());
                viewHolder.getBody().setText(movie.getDescription());
            }

        }
    }
}
