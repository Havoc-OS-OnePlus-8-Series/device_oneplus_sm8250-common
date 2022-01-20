package org.havoc.device.DeviceSettings.Services;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;

import org.havoc.device.DeviceSettings.DeviceSettings;
import org.havoc.device.DeviceSettings.ModeSwitch.EdgeTouchSwitch;
import org.havoc.device.DeviceSettings.Utils.Utils;

@TargetApi(24)
public class EdgeTouchTileService extends TileService {
    private boolean enabled = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        enabled = EdgeTouchSwitch.isCurrentlyEnabled(this);
        getQsTile().setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();

    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        enabled = EdgeTouchSwitch.isCurrentlyEnabled(this);
        Utils.writeValue(EdgeTouchSwitch.getFile(), enabled ? "0" : "1");
        sharedPrefs.edit().putBoolean(DeviceSettings.KEY_EDGE_TOUCH, enabled ? false : true).commit();
        getQsTile().setState(enabled ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
        getQsTile().updateTile();
    }
}