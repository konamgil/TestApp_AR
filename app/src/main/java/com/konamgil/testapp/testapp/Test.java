package com.konamgil.testapp.testapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import eu.kudan.kudan.ARAPIKey;
import eu.kudan.kudan.ARActivity;
import eu.kudan.kudan.ARArbiTrack;
import eu.kudan.kudan.ARGyroPlaceManager;
import eu.kudan.kudan.ARImageNode;
import eu.kudan.kudan.ARLightMaterial;
import eu.kudan.kudan.ARMeshNode;
import eu.kudan.kudan.ARModelImporter;
import eu.kudan.kudan.ARModelNode;
import eu.kudan.kudan.ARTexture2D;

/**
 * Created by konamgil on 2017-05-26.
 */

public class Test extends ARActivity {

    final int MY_PERMISSION_REQUEST_SMSSEND = 100;
    private ARModelNode modelNode;
    private ARBITRACK_STATE arbitrack_state;

    //Tracking enum
    enum ARBITRACK_STATE {
        ARBI_PLACEMENT,
        ARBI_TRACKING
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcamera_view);
        arbitrack_state  = ARBITRACK_STATE.ARBI_PLACEMENT;
        checkPermission();
        init();

    }

    @Override
    public void setup() {
        addModelNode();
        setupArbiTrack();
    }

    public void init(){
        ARAPIKey key = ARAPIKey.getInstance();
        key.setAPIKey("KaWFmhPgMl5bK4sXnVj1clBdkkj8n7Kx8xBJS5RaJA+OHTrS6k9j3abagwEighCB3qKfrNy56mKo/p+QbrjqactsUSm/RX7gM2SJYwfckF+RORvkVpKqStDwnVcBKJcF7t9SDHf4vLuRf5aRGNLp9zSeIEteFSUfXep3+Nrt87CFdRc8f4SDwOUn1nNW6nNG70A1V1kE6fYn75O6g2x8/2DW1AlaaGSLrvNj7nwOP3/t0I9EoXzCrnmT9a/CRlGKvF6nDfXenS+TSAVu2CtnjfuQnVI8uBosbwK3aKAFk8AiyMz7jCB/ZpExt+pnXtJR68rZW6H3dhii3aerArTVExciNmB7lXvz8lxbw6AvQJreU7+SpMf7E88a2q9Eu+bHLnP/QQtBBYt2tJ4zrpFkKTnsWAQHuYTh2muoLBUKpn0y6Ey6S+uhkMR9NHNUfH4PbnoU7fV+IKFZfN6krAM/YEm7d6b6bo9GiSHuTyAy4z/1aEP8JBenBpXXtREh8N1cAmK28ttmrzA7jHoPhXjnSlfcSPbYWizh7Zmxg9WCppklefynbOaC7TThcXScyJ6sM7jyV6jEeHYWhEgswXB3b9tvSY3RD6oHgRwV1Xr7JHU+iSHQIcPG9i9pSHoVXNpMNjY4xw2G5CdMvAb+cJWfxTYwIBma1e3K+UHQ81xR4QA=");
    }

    private  void addModelNode() {

        // Import model
        ARModelImporter modelImporter = new ARModelImporter();
        modelImporter.loadFromAsset("ben.jet");
        modelNode = (ARModelNode) modelImporter.getNode();

        // Load model texture
        ARTexture2D texture2D = new ARTexture2D();
        texture2D.loadFromAsset("bigBenTexture.png");

        // Apply model texture to model texture material
        ARLightMaterial material = new ARLightMaterial();
        material.setTexture(texture2D);
        material.setAmbient(0.8f, 0.8f, 0.8f);

        // Apply texture material to models mesh nodes
        for (ARMeshNode meshNode : modelImporter.getMeshNodes()) {
            meshNode.setMaterial(material);
        }

        modelNode.scaleByUniform(0.25f);

    }

    //Sets up arbi track
    public void setupArbiTrack() {

        // Create an image node to be used as a target node
        ARImageNode targetImageNode = new ARImageNode("target2.png");

        // Scale and rotate the image to the correct transformation.
        targetImageNode.scaleByUniform(0.3f);
        targetImageNode.rotateByDegrees(90, 1, 0, 0);

        // Initialise gyro placement. Gyro placement positions content on a virtual floor plane where the device is aiming.
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
        gyroPlaceManager.initialise();

        // Add target node to gyro place manager
        gyroPlaceManager.getWorld().addChild(targetImageNode);

        // Initialise the arbiTracker
        ARArbiTrack arbiTrack = ARArbiTrack.getInstance();
        arbiTrack.initialise();

        // Set the arbiTracker target node to the node moved by the user.
        arbiTrack.setTargetNode(targetImageNode);

        // Add model node to world
        arbiTrack.getWorld().addChild(modelNode);
    }

    public void lockPosition(View view) {

        Button b = (Button)findViewById(R.id.lockButton);
        ARArbiTrack arbiTrack = ARArbiTrack.getInstance();

        // If in placement mode start arbi track, hide target node and alter label
        if(arbitrack_state.equals(ARBITRACK_STATE.ARBI_PLACEMENT)) {

            //Start Arbi Track
            arbiTrack.start();

            //Hide target node
            arbiTrack.getTargetNode().setVisible(false);

            //Change enum and label to reflect Arbi Track state
            arbitrack_state = ARBITRACK_STATE.ARBI_TRACKING;
            b.setText("Stop Tracking");


        }

        // If tracking stop tracking, show target node and alter label
        else {

            // Stop Arbi Track
            arbiTrack.stop();

            // Display target node
            arbiTrack.getTargetNode().setVisible(true);

            //Change enum and label to reflect Arbi Track state
            arbitrack_state = ARBITRACK_STATE.ARBI_PLACEMENT;
            b.setText("Start Tracking");

        }

    }
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

//            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
//                Toast.makeText(this, "SEND_SMS", Toast.LENGTH_SHORT).show();
//            }
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, MY_PERMISSION_REQUEST_SMSSEND);
        } else {
            Toast.makeText(this, "허용", Toast.LENGTH_SHORT).show();
        }
    }
}
