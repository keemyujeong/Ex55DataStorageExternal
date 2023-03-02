package com.kyjsoft.ex55datastorageexternal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    EditText et;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = findViewById(R.id.et);
        tv = findViewById(R.id.tv);

        findViewById(R.id.btn_save).setOnClickListener(view -> saveData());
        // 실행문이 어쨌든 한줄이니까 {}도 생락가능이야. 세미콜론도 지우셈
        findViewById(R.id.btn_load).setOnClickListener(view -> {loadData();});


        // 외부메모리 안에 개발자가 원하는 특정 폴더 위치에 저장하기 -> 앱을 삭제해도 파일이 그대로 유지됨.
        findViewById(R.id.btn).setOnClickListener(view -> clickbtn());





    }

    // 외부저장소의 특정 폴더에 파일 저장하는 기능
    void saveData2(){
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)){
            // 연결되어있으면 파일 저장

            // sdcard의 특정 위치 폴더 경로
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if(path!=null){
                tv.setText(path.getPath());
            }
            File file = new File(path, "aaa.txt");
            try {
                FileWriter fileWriter = new FileWriter(file, true);
                PrintWriter writer = new PrintWriter(fileWriter);

                writer.println(et.getText().toString());
                writer.flush();
                writer.close();

                et.setText("");

                Toast.makeText(this, "다운로드 폴더에 저장 완료", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    void clickbtn(){
        // 외부 메모리의 특정 디렉토리에 파일 저장하려면 사용자에서 허가(permission)를 받는 작업이 필요함.
        // 아무 폴더에 저장하게 해버리면 악성코드 심어버릴 수 있으니까 원래를 외부 메모리의 특정 폴더에 저장하도록 안드로이드가 만든건데 개발자가 그걸 고치고 특정 폴더에 저장할라고 할려면 사용자한테 허락을 받아야함.
        // 동적 퍼미션은 AndroidManifest.xml에서 -> 다이올로그로 허가/거부를 하도록 강제 요청

        // 이미 허가한 적이 있는지 확인
        int checkResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(checkResult == PackageManager.PERMISSION_DENIED){
            // 현재 퍼미션이 거부 상태 인가( 디폴트가 거부임 -> 버튼 누르면 디폴트가 거부니까 일단 다이올로그 띄우는 거야.)
            // 퍼미션을 오청하는 다이올로그를 보여주는 기능 메소드 호출하기
            // 다이올로그의 형태는 운영체제가 만들어줌-> builder 만들지마
            String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}; // 허가가 여러 개있을 수 있으니까 배열로 받음.
            requestPermissions(permission, 10); // 니가 요청한 10이 결과에서 10이랑 같으면 연결되는거임
        } else{
            saveData2();
        }


    }

    // requestPermissions메소드 호출로 보여지는 다이올로그에서 허가 또는 거부의 선택에 따라 자동으로 발동하는 콜백 메소드가 있음.
    // 다이올로그 2번 연달아 허용안함 하면 다신 안물어봄


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==10){

            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) { // 허가의 결과가 grantResults 허가되었으면
                Toast.makeText(this, "외부저장소 저장 사용 가능", Toast.LENGTH_SHORT).show();
                saveData2();
            }else{
                Toast.makeText(this, "외부저장소 저장 금지, 기능 사용 불가", Toast.LENGTH_SHORT).show();
            }

        }
    }

    void saveData(){

        // 외부메모리(SDcard, USB)가 있니? 라고 확인해주기 -> 없을 수도 있으니까.
        String state = Environment.getExternalStorageState(); // 외부상태를 가져와(리턴 값이 있음)

        // 외장메모리 상태(state)가 연결(mounted(장착))되어있지 않은가? -> 외부메모리가 없는 상태
        if(!state.equals(Environment.MEDIA_MOUNTED)){ //unmounted 쓰지마
            Snackbar.make(tv, "sdcard is not mounted", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // 저장할 데이터를 EditText로 부터 가져오기
        String data = et.getText().toString();
        et.setText("");

        // 외부저장소에 파일명,확장자 정해서 저장하기
        // 외장메모리는 운영체제가 스트림을 열 수 있는 것이 아님 -> 파일의 저장경로부터 준비해주기(디렉토리)
        // 외부저장소에 data를 저장할 수 있는 곳을(접근할 수 있는 디렉토리를) 안드로이드가 딱 정해놈. -> 마시멜로우버전 이상 부터
        // -> 근데 삼성, 샤오미 등등 다 그 디렉토리 경로가 다름. 그래서 그 경로를 감지하고 경로를 설정해줘야함.
        File path; // 외부메모리 경로를 관리하는 참조변수

        // 앱에게 각자 할당된 경로(storage/emulated/0/Android/data/내 패키지/files -> 내꺼 애뮬레이터)안에 저장할 거임.
        // 그냥 넣지 말고 그 안에 구분하기 좋게 files안에 myDir폴더를 만들고
        File[] paths = getExternalFilesDirs("Mydir"); // 경로 안에 data폴더 만드는 거임.
        path = paths[0];
        tv.setText(path.getPath()); // 경로확인 용

        // 위 경로에다 파일을 만들기 위한 파일객체 만들기 File객체 생성
        File file = new File(path,"Data.txt"); // 얘는 inputstream안쓰고 바로 보조스트림으로 보내도 됨.
        // 그래서 file 클래스 객체를 만든거임

        try {
            FileWriter fw = new FileWriter(file, true); // 덮어쓰기 할거임.
            PrintWriter writer = new PrintWriter(fw);

            writer.println(data); // et에 써있는 글씨

            writer.flush();
            writer.close();

            Snackbar.make(tv, "저장완료", Snackbar.LENGTH_SHORT).show();

            // 소프트키보드를 안 보이도록 -> 입력관리자 객체 가져오기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0); // 토큰을 가진 애가 소프트키보드의 입력(제어을 할 수 있는)을 받도록
            // 현재 토큰을 가지고 있는 포커스누구임, 토큰 내놔.
            // 버튼 누르지 않아도 자동으로 내려감.

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void loadData(){

        // 외장메모리 상태 읽을 수 있는 상태인지
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
            // 연결되었거나 읽기전용으로 연결되었거나
            File[] paths = getExternalFilesDirs("Mydir"); // 파일 경로 얻어오기
            File path = paths[0];

            File file = new File(path, "Data.txt");

            try {
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);

                StringBuffer buffer = new StringBuffer();
                while (true){

                    String line = reader.readLine();
                    if( line == null ) break;
                    buffer.append(line+"\n");

                }

                tv.setText(buffer);
                reader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}

// 외부메모리도 앱 삭제하면 메모리에서 없에버림.

