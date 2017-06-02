package tw.edu.ntust.jojllman.wearableapplication;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Vibrator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import tw.edu.ntust.jojllman.wearableapplication.BLE.MjpegView;

/**
 * Created by Lian on 2015/11/26.
 */
public class GlobalVariable extends Application {
    public static final String defaultNameGlove = "GloveBLE";
    public static final String defaultNameGlass = "UltraSound";
    public static final String defaultNameBracelet = "Nordic_Bracelet";

    public static final int VIBRATE_LIGHT=1;
    public static final int VIBRATE_MID=2;
    public static final int VIBRATE_STRONG=4;
    public static String braceletAddress = "";
    public static String glassesAddress = "";
    public static String glassesIPAddress ="";
    private boolean _isSettingChanged=false;
    private int vibrate_level=VIBRATE_MID;
    private int glass_front_threshold = 100;
    private int glass_side_threshold = 100;

    private boolean bracelet_distance_enabled = false;
    private boolean bracelet_color_enabled = false;

    private SavedDevices saved_devices;

    public GlobalVariable(){
        saved_devices = new SavedDevices();
    }

    public MjpegView mv = null;



    public void setVibrate_level(int new_vibrate_level){
        if(new_vibrate_level != vibrate_level) {
            vibrate_level = new_vibrate_level;
            _isSettingChanged = true;
        }
    }

    public void setGlassFrontThreshold(int new_front_value){
        if(new_front_value != glass_front_threshold) {
            glass_front_threshold = new_front_value;
            _isSettingChanged = true;
        }
    }

    public void setGlassSideThreshold(int new_side_value){
        if(new_side_value != glass_side_threshold) {
            glass_side_threshold = new_side_value;
            _isSettingChanged = true;
        }
    }

    public void setBraceletDistanceEnabled(boolean bracelet_distance_enabled) {
        if(bracelet_distance_enabled != this.bracelet_distance_enabled) {
            this.bracelet_distance_enabled = bracelet_distance_enabled;
            _isSettingChanged = true;
        }
    }

    public void setBraceletColorEnabled(boolean bracelet_color_enabled) {
        if(bracelet_color_enabled != this.bracelet_color_enabled) {
            this.bracelet_color_enabled = bracelet_color_enabled;
            _isSettingChanged = true;
        }
    }

    public int getVibrate_level(){
        return vibrate_level;
    }

    public int getGlassFrontThreshold() {
        return glass_front_threshold;
    }

    public int getGlassSideThreshold() {
        return glass_side_threshold;
    }

    public SavedDevices getSaved_devices(){
        return saved_devices;
    }

    public boolean isBraceletDistanceEnabled() {
        return bracelet_distance_enabled;
    }

    public boolean isBraceletColorEnabled() {
        return bracelet_color_enabled;
    }

    public boolean isSettingChanged(){
        return _isSettingChanged;
    }

    public void vibrate(int duration){
        if(duration>1) {
            Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vi.cancel();
            long pattern[] = new long[duration];
            for (int i = 0; i < pattern.length; i += 2) {
                pattern[i] = 10 / vibrate_level;
                pattern[i + 1] = 2*vibrate_level+1;
            }
            vi.vibrate(pattern, -1);
        }
    }

    public boolean saveSetting(){
        _isSettingChanged=false;
        FileOutputStream out;
        try {
            System.out.println("start saving settings.");
            //在 getFilesDir() 目錄底下建立 setting.txt 檔案用來進行寫入
            out = openFileOutput("setting.txt", Context.MODE_PRIVATE);

            String outstr="";
            outstr+="VIBRATE_LEVEL="+vibrate_level+"\n";
            outstr+="GLASS_FRONT_THRESHOLD="+glass_front_threshold+"\n";
            outstr+="GLASS_SIDE_THRESHOLD="+glass_side_threshold+"\n";
            outstr+="BRACELET_DISTANCE_ENABLED="+bracelet_distance_enabled+"\n";
            outstr+="BRACELET_COLOR_ENABLED="+bracelet_color_enabled+"\n";

            //將資料寫入檔案中
            out.write(outstr.getBytes());
            out.flush();

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            saved_devices.saveDevice();
            return false;
        }
        return saved_devices.saveDevice();
    }

    public boolean readSetting(){
        _isSettingChanged=false;
        FileInputStream in;
        StringBuffer data = new StringBuffer();
        try {
            System.out.println("opening setting.txt");
            //開啟 getFilesDir() 目錄底下名稱為 setting.txt 檔案
            in = openFileInput("setting.txt");

            System.out.println("start reading settings.");
            //讀取該檔案的內容
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line + "\n");
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            saved_devices.loadDevice();
            return false;
        }
        int current_pos=data.indexOf("VIBRATE_LEVEL");
        if(current_pos>=0){
            vibrate_level=Integer.parseInt(data.substring(data.indexOf("=", current_pos) + 1, data.indexOf("\n", current_pos)));
        }else vibrate_level = VIBRATE_MID;

        current_pos=data.indexOf("GLASS_FRONT_THRESHOLD");
        if(current_pos>=0){
            glass_front_threshold=Integer.parseInt(data.substring(data.indexOf("=", current_pos) + 1, data.indexOf("\n", current_pos)));
        }else glass_front_threshold = 100;

        current_pos=data.indexOf("GLASS_SIDE_THRESHOLD");
        if(current_pos>=0){
            glass_side_threshold=Integer.parseInt(data.substring(data.indexOf("=", current_pos) + 1, data.indexOf("\n", current_pos)));
        }else glass_side_threshold = 100;

        current_pos=data.indexOf("BRACELET_DISTANCE_ENABLED");
        if(current_pos>=0){
            bracelet_distance_enabled=Boolean.parseBoolean(data.substring(data.indexOf("=", current_pos) + 1, data.indexOf("\n", current_pos)));
        }else bracelet_distance_enabled = false;

        current_pos=data.indexOf("BRACELET_COLOR_ENABLED");
        if(current_pos>=0){
            bracelet_color_enabled=Boolean.parseBoolean(data.substring(data.indexOf("=", current_pos) + 1, data.indexOf("\n", current_pos)));
        }else bracelet_color_enabled = false;

        return saved_devices.loadDevice();
    }

    public class SavedDevices{
        private HashMap<String, String> device_address = new HashMap<>();

        public void addDevice(String name, String addr) {
            if(!device_address.containsKey(name)) {
                device_address.put(name,addr);
                saveDevice();
            }
        }

        public String[] getDeviceList() {
            return device_address.keySet().toArray(new String[device_address.size()]);
        }

        public String getDevice(String name) {
            return device_address.get(name);
        }

        public boolean containsDeviceName(String name) {
            return device_address.containsKey(name);
        }

        public boolean containsDeviceAddr(String addr) {
            return device_address.containsValue(addr);
        }

        public boolean removeDevice(String name){
            String addr = device_address.remove(name);
            if(addr == null || addr == "")return false;
            saveDevice();
            return true;
        }

        private boolean saveDevice(){
            FileOutputStream outDev;
            try {
                System.out.println("start saving devices.");
                outDev = openFileOutput("device.txt", Context.MODE_PRIVATE);

                String outstr2="";
                for (String dev:device_address.keySet()) {
                    outstr2+=dev+"="+device_address.get(dev)+"\n";
                }

                outDev.write(outstr2.getBytes());
                outDev.flush();

                outDev.close();
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public boolean loadDevice(){
            FileInputStream inDev;
            try {
                System.out.println("opening device.txt");
                inDev = openFileInput("device.txt");

                System.out.println("start reading devices.");
                //讀取DEVICE
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inDev, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if(line.endsWith("\n"))line=line.substring(0,line.length()-1);
                    String temp[] = line.split("=");
                    device_address.put(temp[0],temp[1]);

                    System.out.println(line);
                }
                System.out.println(device_address.size() + " devices read.");
                inDev.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    public boolean initDevice(){
        FileOutputStream outDev;
        try {
            System.out.println("start saving devices.");
            outDev = openFileOutput("device.txt", Context.MODE_PRIVATE);

            outDev.write(Integer.parseInt(""));

            outDev.close();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static boolean isServiceRunning(Context context, String serviceClassName){
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }
}
