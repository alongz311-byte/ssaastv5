package my.edu.ssaas.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Buka SSAASTV automatik bila Mi Box dihidupkan. */
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
            Intent a = new Intent(c, MainActivity.class);
            a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(a);
        }
    }
}
