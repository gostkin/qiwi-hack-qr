package org.chainify.qiwi_blockchain;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;

public class ApprovalMenu extends Fragment {
    Button generateKeyBtn;
    Button verifyBtn;

    EditText passwordEdit;
    EditText pk, sk;
    EditText passportEdit;

    RelativeLayout generationLayout;

    LinearLayout verificationLayout, passportLayout;

    private void updateVisibility(Context ctx) {
        if (!SaveSharedPreference.getPasswordHash(ctx).isEmpty()) {
            generationLayout.setVisibility(View.INVISIBLE);
        } else {
            verificationLayout.setVisibility(View.INVISIBLE);
            passportLayout.setVisibility(View.INVISIBLE);
        }

        if (SaveSharedPreference.getVerified(ctx)) {
            passportLayout.setVisibility(View.INVISIBLE);
        } else {
            passportLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateKeys(Context ctx) {
        if (!SaveSharedPreference.getPasswordHash(ctx).isEmpty()) {
            pk.setText(SaveSharedPreference.getEncryptedPK(ctx));
            sk.setText(SaveSharedPreference.getEncryptedSK(ctx));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        RelativeLayout thisView = (RelativeLayout) inflater.inflate(R.layout.fragment_approval_menu, container, false);
        Context ctx = thisView.getContext();


        System.out.println("LOL" + SaveSharedPreference.getEncryptedPK(ctx) + SaveSharedPreference.getEncryptedSK(ctx));
        generationLayout = thisView.findViewById(R.id.generation_layout);

        verificationLayout = thisView.findViewById(R.id.verification_layout);
        passportLayout = thisView.findViewById(R.id.passport_layout);

        generateKeyBtn = thisView.findViewById(R.id.generate_btn);

        passwordEdit = thisView.findViewById(R.id.password_ver_edit);
        passportEdit = thisView.findViewById(R.id.passport_edit);
        verifyBtn = thisView.findViewById(R.id.verify_btn);

        pk = thisView.findViewById(R.id.pk_edit);
        sk = thisView.findViewById(R.id.sk_edit);

        generateKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                generateAndSaveKeys(ctx, passwordEdit.getText().toString());

                generationLayout.setVisibility(View.INVISIBLE);
                verificationLayout.setVisibility(View.VISIBLE);

                updateKeys(ctx);
            }
        });

        updateVisibility(ctx);
        updateKeys(ctx);

        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return thisView;
    }

    public void generateAndSaveKeys(Context context, String password) {
        ECKeyPair pair = ECKeyPair.createNew(false);
        SaveSharedPreference.setKeys(context, pair, password);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Approval menu");
    }
}