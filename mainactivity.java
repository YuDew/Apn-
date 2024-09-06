package cheng.android.apn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;

    private TextView statusTextView;
    private Button configureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        configureButton = findViewById(R.id.configureButton);

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_SETTINGS
            }, REQUEST_CODE_PERMISSION);
        }

        configureButton.setOnClickListener(v -> configureAPN());
    }

    private void configureAPN() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String operatorNumeric = telephonyManager.getSimOperator();

        String apn = "";
        String apnType = "default,supl";
        String apnProtocol = "IPV4/IPv6";
        String apnRoamingProtocol = "IPV4/IPv6";

        switch (operatorNumeric) {
            case "46000":
            case "46002":
            case "46004":
            case "46007":
                apn = "cmnet";
                break;
            case "46001":
            case "46006":
                apn = "uninet";
                break;
            case "46003":
                apn = "ctnet";
                break;
            case "46015":
                apn = "cbnet";
                break;
            default:
                Toast.makeText(this, "未识别的运营商", Toast.LENGTH_SHORT).show();
                return;
        }


        try {

            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());


            String command = String.format("content insert --uri content://telephony/carriers --bind name:text 'Auto Config' --bind apn:text '%s' --bind type:text '%s' --bind protocol:text '%s' --bind roaming_protocol:text '%s'\n", apn, apnType, apnProtocol, apnRoamingProtocol);
            outputStream.writeBytes(command);
            outputStream.writeBytes("exit\n");
            outputStream.flush();

            process.waitFor();

            Toast.makeText(this, "APN配置成功", Toast.LENGTH_SHORT).show();
            statusTextView.setText("当前APN配置: " + apn);
        } catch (IOException | InterruptedException e) {
            Toast.makeText(this, "APN配置失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
