package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cn.jpush.android.api.JPushInterface;

import static com.kongx.nkuassistant.Information.COURSE_PREFS_NAME;
import static com.kongx.nkuassistant.Information.EXAM_PREFS_NAME;
import static com.kongx.nkuassistant.Information.PREFS_NAME;
import static com.kongx.nkuassistant.Information.Strings;
import static com.kongx.nkuassistant.Information.WEB_URL;
import static com.kongx.nkuassistant.Information.bugCheckFile;
import static com.kongx.nkuassistant.Information.curriculum_lastUpdate;
import static com.kongx.nkuassistant.Information.examCount;
import static com.kongx.nkuassistant.Information.exams;
import static com.kongx.nkuassistant.Information.facultyName;
import static com.kongx.nkuassistant.Information.id;
import static com.kongx.nkuassistant.Information.ids;
import static com.kongx.nkuassistant.Information.ifRemPass;
import static com.kongx.nkuassistant.Information.majorName;
import static com.kongx.nkuassistant.Information.name;
import static com.kongx.nkuassistant.Information.selectedCourseCount;
import static com.kongx.nkuassistant.Information.selectedCourses;
import static com.kongx.nkuassistant.Information.studiedCourseCount;
import static com.kongx.nkuassistant.Information.weekdays_tobalitai;
import static com.kongx.nkuassistant.Information.weekdays_tojinnan;
import static com.kongx.nkuassistant.Information.weekends_tobalitai;
import static com.kongx.nkuassistant.Information.weekends_tojinnan;

public class WelcomeActivity extends AppCompatActivity {

    private boolean componentReady = false;
    private Intent startIntent = null;
    final TimerTask timerTask = new TimerTask() {
        public void run() {
            synchronized (this){
                while (!componentReady) try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WelcomeActivity.this.startActivity(startIntent);
            WelcomeActivity.this.finish();
        }
    };

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        View v = View.inflate(getApplicationContext(),R.layout.activity_welcome,null);
        setContentView(v);
        new Timer().schedule(timerTask, 2000);


        ImageView startImg1 = (ImageView)v.findViewById(R.id.startImg);
        ImageView startImg2 = (ImageView)v.findViewById(R.id.startImg2);
        startImg1.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.splash_fade_in));
        startImg2.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.splash_move_in));

        JPushInterface.setDebugMode(true);
        JPushInterface.init(getApplication());

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        System.setProperty("java.net.useSystemProxies", "true");
        CookieManager cookieManager = new CookieManager();
        Connect.initialize(cookieManager);
        bugCheckFile = getSharedPreferences(Information.PREFS_NAME,0).getString("lastBugCheckFile",null);

        ifRemPass = settings.getBoolean(Strings.setting_remember_pwd, false);
        studiedCourseCount = settings.getInt(Strings.setting_studied_course_count, -1);
        name = settings.getString(Strings.setting_student_name, "Name");
        facultyName = settings.getString(Strings.setting_student_faculty, "Faculty");
        id = settings.getString(Strings.setting_studentID, "ID");
        ids = settings.getString(Strings.setting_studentIDs,"IDs");
        majorName = settings.getString(Strings.setting_student_major, "Major");
        if(ifRemPass) {
            startIntent = new Intent(WelcomeActivity.this, IndexActivity.class);
            try {
                HttpCookie cookie = new HttpCookie("JSESSIONID", settings.getString("JSESSIONID", ""));
                cookie.setDomain("222.30.49.10");
                cookie.setPath("/");
                cookie.setVersion(0);
                cookieManager.getCookieStore().add(new URI(WEB_URL + "/"), cookie);
            } catch (URISyntaxException e) {
                Log.e("WelcomeActivity", "Caught URISyntaxException");
            }
        }else startIntent = new Intent(WelcomeActivity.this, EduLoginActivity.class);

        //get Curriculum Preferences
        settings = getSharedPreferences(COURSE_PREFS_NAME,0);
        selectedCourseCount = settings.getInt(Strings.setting_selected_course_count, -1);
        curriculum_lastUpdate = settings.getString(Strings.setting_last_update_time, null);
        selectedCourses = new ArrayList<>();
        if(selectedCourseCount != -1){
            CourseSelected tmpCourse;
            for(int i = 0;i < selectedCourseCount;i++){
                tmpCourse = new CourseSelected();
                tmpCourse.index = settings.getString("index"+i,null);
                tmpCourse.name = settings.getString("name"+i,null);
                tmpCourse.dayOfWeek = Integer.parseInt(settings.getString("dayOfWeek"+i,null));
                tmpCourse.startTime  = Integer.parseInt(settings.getString("startTime"+i,null));
                tmpCourse.endTime = Integer.parseInt(settings.getString("endTime"+i,null));
                tmpCourse.classRoom = settings.getString("classRoom"+i,null);
//                tmpCourse.classType = settings.getString("classType"+i,null));
                tmpCourse.teacherName = settings.getString("teacherName"+i,null);
                tmpCourse.startWeek = Integer.parseInt(settings.getString("startWeek"+i,null));
                tmpCourse.endWeek = Integer.parseInt(settings.getString("endWeek"+i,null));
                selectedCourses.add(tmpCourse);
            }
        }

        //get Exams Preferences
        settings = getSharedPreferences(EXAM_PREFS_NAME,0);
        examCount = settings.getInt(Strings.setting_exam_count, -1);
        if(examCount != -1){
            HashMap<String,String> map;
            for(int i = 0;i < examCount;i++){
                map = new HashMap<>();
                map.put("name",settings.getString("name"+i,null));
                map.put("startTime",settings.getString("startTime"+i,null));
                map.put("endTime",settings.getString("endTime"+i,null));
                map.put("classRoom",settings.getString("classRoom"+i,null));
                map.put("date",settings.getString("date"+i,null));
                exams.add(map);
            }
        }

        readBusFile();
        componentReady = true;
        synchronized (timerTask) {
            timerTask.notifyAll();
        }
    }

    private void readBusFile(){
        Element element = null;
        InputStream inputStream  = null;
        try{
            inputStream = getAssets().open("timetable.xml");
        }catch (Exception e){
            Log.e("WelcomeActivity", "Open bus file failed.");
        }
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = null;
        weekdays_tojinnan = new ArrayList<>();
        weekdays_tobalitai = new ArrayList<>();
        weekends_tojinnan = new ArrayList<>();
        weekends_tobalitai = new ArrayList<>();
        try{
            documentBuilderFactory  = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            element  = document.getDocumentElement();
            NodeList days = element.getChildNodes();
            NodeList weekdays_tojinnan_list = days.item(1).getChildNodes().item(1).getChildNodes();
            NodeList weekdays_tobalitai_list = days.item(1).getChildNodes().item(3).getChildNodes();
            NodeList weekends_tojinnan_list = days.item(3).getChildNodes().item(1).getChildNodes();
            NodeList weekends_tobalitai_list = days.item(3).getChildNodes().item(3).getChildNodes();
            HashMap<String,Integer> tmpMap;
            for(int i = 0;i<weekdays_tojinnan_list.getLength();i++){
                Node node  = weekdays_tojinnan_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekdays_tojinnan.add(tmpMap);
                    Log.e("BUS",tmpMap.get("id")+"");
                }
            }
            for(int i = 0;i<weekdays_tobalitai_list.getLength();i++){
                Node node  = weekdays_tobalitai_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekdays_tobalitai.add(tmpMap);
                }
            }
            for(int i = 0;i<weekends_tojinnan_list.getLength();i++){
                Node node  = weekends_tojinnan_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekends_tojinnan.add(tmpMap);
                }
            }
            for(int i = 0;i<weekends_tobalitai_list.getLength();i++){
                Node node  = weekends_tobalitai_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekends_tobalitai.add(tmpMap);
                }
            }
        } catch (SAXException|ParserConfigurationException|IOException e) {
            Log.e("WelcomeActivity", e.toString());
        }
    }
}
