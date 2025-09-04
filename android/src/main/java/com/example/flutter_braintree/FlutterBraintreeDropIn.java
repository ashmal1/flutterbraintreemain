package com.example.flutterbraintreemain;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.DropInResultCallback;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.UserCanceledException;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class FlutterBraintreeDropIn implements FlutterPlugin, ActivityAware, MethodCallHandler, ActivityResultListener {

    private static final String CHANNEL_NAME = "flutterbraintreemain.drop_in";
    private MethodChannel channel;
    private Activity activity;
    private DropInClient dropInClient;
    private BraintreeClient braintreeClient;

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
        if (call.method.equals("startDropIn")) {
            String token = call.argument("authorization");
            startDropIn(token, result);
        } else {
            result.notImplemented();
        }
    }

    private void startDropIn(String authorization, Result result) {
        if (activity == null) {
            result.error("NO_ACTIVITY", "Activity is null", null);
            return;
        }

        braintreeClient = new BraintreeClient(activity, authorization);
        dropInClient = new DropInClient(activity, braintreeClient);

        DropInRequest dropInRequest = new DropInRequest();
        dropInClient.launchDropIn(dropInRequest, (dropInResult, error) -> {
            if (error != null) {
                result.error("BRAINTREE_ERROR", error.getMessage(), null);
            } else if (dropInResult != null) {
                PaymentMethodNonce nonce = dropInResult.getPaymentMethodNonce();
                if (nonce != null) {
                    result.success(nonce.getString());
                } else {
                    result.error("NO_NONCE", "No payment method nonce returned", null);
                }
            } else {
                result.error("CANCELLED", "User cancelled", null);
            }
        });
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        // If you have any activity result to handle for legacy reasons, handle it here.
        return false;
    }
}
