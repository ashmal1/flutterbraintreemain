package com.example.flutter_braintree;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.PaymentMethodNonce;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class FlutterBraintreeDropIn implements FlutterPlugin, ActivityAware, MethodCallHandler, ActivityResultListener {

    private static final String CHANNEL_NAME = "flutter_braintree.drop_in";
    private MethodChannel channel;
    private Activity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("startDropIn".equals(call.method)) {
            String token = call.argument("authorization");
            startDropIn(token, result);
        } else {
            result.notImplemented();
        }
    }

    private void startDropIn(String authorization, Result result) {
    if (!(activity instanceof FragmentActivity)) {
        result.error("INVALID_ACTIVITY", "Activity is not a FragmentActivity", null);
        return;
    }

    FragmentActivity fragmentActivity = (FragmentActivity) activity;
    DropInClient dropInClient = new DropInClient(fragmentActivity, authorization);
    DropInRequest dropInRequest = new DropInRequest();

    // Set result listener BEFORE launching
    dropInClient.setListener(dropInResult -> {
        if (dropInResult.getError() != null) {
            result.error("BRAINTREE_ERROR", dropInResult.getError().getMessage(), null);
        } else if (dropInResult.getPaymentMethodNonce() != null) {
            result.success(dropInResult.getPaymentMethodNonce().getString());
        } else {
            result.error("CANCELLED", "User cancelled or no payment method selected", null);
        }
    });

    // Launch without callback
    dropInClient.launchDropIn(dropInRequest);
}


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        // No legacy handling needed
        return false;
    }
}
