package com.androideasyapps.phoenix.shared.prefs;

/**
 * Created by seans on 10/01/15.
 */
public interface PhoenixPreferences {
    /**
     * Limits the number of items to show for "recent" querries
     * NOTE: relates to the "recent_limit" key in preferences.xml
     * @return
     */
    public int recent_limit();
    public void recent_limit(int limit);

    /**
     * Limits the number of items to show for "recommendations"
     * NOTE: relates to the "recommendation_limit" key in preferences.xml
     * @return
     */
    public int recommendation_limit();
    public void recommendation_limit(int limit);

}
