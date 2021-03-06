package com.example.ideal.myapplication.fragments.foundElements;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.objects.Service;
import com.example.ideal.myapplication.other.GuestService;

@SuppressLint("ValidFragment")
public class foundServiceProfileElement extends Fragment implements View.OnClickListener {

    final String SERVICE_ID = "service id";

    TextView nameText;

    String idString;
    String nameString;

    @SuppressLint("ValidFragment")
    public foundServiceProfileElement(Service service) {
        idString = service.getId();
        nameString = service.getName();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.found_service_profile_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        nameText = view.findViewById(R.id.serviceNameFoundServiceProfileElementText);
        nameText.setOnClickListener(this);
        setData();
    }

    private void setData() {
        nameText.setText(nameString);
    }

    @Override
    public void onClick(View v) {
        goToGuestService();
    }

    private void goToGuestService(){
        Intent intent = new Intent(this.getContext(), GuestService.class);
        intent.putExtra(SERVICE_ID, idString);
        startActivity(intent);
    }
}
