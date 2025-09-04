package com.example.flutter_braintree;

import android.app.Activity;
import android.content.Intent;

import java.util.Map;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class FlutterBraintreePlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, ActivityResultListener {
    private static final int CUSTOM_ACTIVITY_REQUEST_CODE = 0x420;

    private Activity activity;
    private Result activeResult;
    private FlutterBraintreeDropIn dropIn;
    private MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_braintree");
        channel.setMethodCallHandler(this);

        dropIn = new FlutterBraintreeDropIn();
        dropIn.onAttachedToEngine(binding); // Keep this only if DropIn also implements FlutterPlugin
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
        if (dropIn != null) {
            dropIn.onDetachedFromEngine(binding);
            dropIn = null;
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        if (dropIn != null) {
            dropIn.onAttachedToActivity(binding);
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
        if (dropIn != null) {
            dropIn.onDetachedFromActivity();
        }
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        if (dropIn != null) {
            dropIn.onReattachedToActivityForConfigChanges(binding);
        }
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
        if (dropIn != null) {
            dropIn.onDetachedFromActivity();
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (activeResult != null) {
            result.error("already_running", "Cannot launch another custom activity while one is already running.", null);
            return;
        }

        if (activity == null) {
            result.error("no_activity", "Plugin is not attached to an activity.", null);
            return;
        }

        activeResult = result;

        switch (call.method) {
            case "tokenizeCreditCard": {
                Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
                intent.putExtra("type", "tokenizeCreditCard");
                intent.putExtra("authorization", (String) call.argument("authorization"));
                Map<?, ?> request = (Map<?, ?>) call.argument("request");
                if (request != null) {
                    intent.putExtra("cardNumber", (String) request.get("cardNumber"));
                    intent.putExtra("expirationMonth", (String) request.get("expirationMonth"));
                    intent.putExtra("expirationYear", (String) request.get("expirationYear"));
                    intent.putExtra("cvv", (String) request.get("cvv"));
                    intent.putExtra("cardholderName", (String) request.get("cardholderName"));
                }
                activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
                break;
            }
            case "requestPaypalNonce": {
                Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
                intent.putExtra("type", "requestPaypalNonce");
                intent.putExtra("authorization", (String) call.argument("authorization"));
                Map<?, ?> request = (Map<?, ?>) call.argument("request");
                if (request != null) {
                    intent.putExtra("amount", (String) request.get("amount"));
                    intent.putExtra("currencyCode", (String) request.get("currencyCode"));
                    intent.putExtra("displayName", (String) request.get("displayName"));
                    intent.putExtra("payPalPaymentIntent", (String) request.get("payPalPaymentIntent"));
                    intent.putExtra("payPalPaymentUserAction", (String) request.get("payPalPaymentUserAction"));
                    intent.putExtra("billingAgreementDescription", (String) request.get("billingAgreementDescription"));
                }
                activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
                break;
            }
            default:
                activeResult = null;
                result.notImplemented();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (activeResult == null) return false;

        if (requestCode == CUSTOM_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String type = data.getStringExtra("type");
                if ("paymentMethodNonce".equals(type)) {
                    activeResult.success(data.getSerializableExtra("paymentMethodNonce"));
                } else {
                    activeResult.error("error", "Invalid activity result type.", null);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                activeResult.success(null);
            } else {
                Exception error = (Exception) data.getSerializableExtra("error");
                activeResult.error("error", error != null ? error.getMessage() : "Unknown error", null);
            }
            activeResult = null;
            return true;
        }
        return false;
    }
}
