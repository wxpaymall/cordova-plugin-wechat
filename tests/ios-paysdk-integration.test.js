const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const root = path.resolve(__dirname, '..');
const pluginXml = fs.readFileSync(path.join(root, 'plugin.xml'), 'utf8');
const header = fs.readFileSync(path.join(root, 'src/ios/CDVWechat.h'), 'utf8');
const implementation = fs.readFileSync(path.join(root, 'src/ios/CDVWechat.m'), 'utf8');

test('iOS AppPay uses vendored PaySDK and PaySDK-first callbacks', () => {
  const paymentMethod = implementation.match(
    /- \(void\)sendPaymentRequest:[\s\S]*?(?=\n- \(void\)entrustAppSignContract:)/
  );

  assert.ok(paymentMethod, 'sendPaymentRequest implementation must exist');
  assert.match(pluginXml, /WechatPaySDK1\.0\.0\/libWechatPaySDK\.a/);
  assert.match(header, /WXPayResultHandler/);
  assert.match(implementation, /\[WXPayKit initWithConfig:/);
  assert.match(implementation, /\[WXPayKit send:req completion:/);
  assert.match(implementation, /handleOpenURL:url handler:self/);
  assert.match(implementation, /handleUniversalLink:userActivity handler:self/);
  assert.match(implementation, /@"fromAppQrPay"/);
  assert.doesNotMatch(paymentMethod[0], /\[WXApi sendReq:/);
});
