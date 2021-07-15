package com.example.todo;

import ohos.ace.ability.AceAbility;
import ohos.aafwk.content.Intent;

public class MainAbility extends AceAbility {
    @Override
    public void onStart(Intent intent) {

        super.onStart(intent);

        requestPermissionsFromUser(new String[]{"ohos.permission.DISTRIBUTED_DATASYNC"}, 0);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
