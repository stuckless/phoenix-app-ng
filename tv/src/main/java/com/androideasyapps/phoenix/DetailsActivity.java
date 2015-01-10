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

import android.app.Activity;
import android.content.ContentUris;
import android.os.Bundle;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.shared.AppInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
public class DetailsActivity extends Activity {
    static final Logger log = LoggerFactory.getLogger(DetailsActivity.class);

    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String MOVIE = "Movie";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // normally mediafile is saved in the AppInstance state, but if not, then
        // get it from the intent.
        MediaFile mf = (MediaFile) getIntent().getSerializableExtra(MOVIE);

        if (mf==null) {
            if (getIntent().getData() != null) {
                log.warn("SEARCH DATA URI: " + getIntent().getData());
                try {
                    mf = AppInstance.getInstance(this).getDAOManager().get().getMediaFileDAO().queryFirst("mediafileid=?", ContentUris.parseId(getIntent().getData()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (mf!=null) {
            AppInstance.getInstance(this).setSelectedMediaFile(mf);
        }

        setContentView(R.layout.activity_details);
    }

}
