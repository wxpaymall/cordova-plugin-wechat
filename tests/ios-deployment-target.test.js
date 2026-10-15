const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const test = require('node:test');

test('iOS prepare hook raises app and CordovaLib deployment targets to 12.0', async () => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'wechat-ios-target-'));
  const projectFiles = [
    'platforms/ios/Test.xcodeproj/project.pbxproj',
    'platforms/ios/CordovaLib/CordovaLib.xcodeproj/project.pbxproj',
  ];
  for (const relativePath of projectFiles) {
    const file = path.join(root, relativePath);
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(
      file,
      'IPHONEOS_DEPLOYMENT_TARGET = 11.0;\n' +
        'IPHONEOS_DEPLOYMENT_TARGET = 10.0;\n' +
        'IPHONEOS_DEPLOYMENT_TARGET = 13.0;\n'
    );
  }

  const enforceDeploymentTarget = require('../scripts/ios-deployment-target');
  await enforceDeploymentTarget({
    opts: { projectRoot: root, platforms: ['ios'] },
  });

  for (const relativePath of projectFiles) {
    const content = fs.readFileSync(path.join(root, relativePath), 'utf8');
    assert.doesNotMatch(content, /IPHONEOS_DEPLOYMENT_TARGET = (?:10|11)\.0/);
    assert.equal(
      content.match(/IPHONEOS_DEPLOYMENT_TARGET = 12\.0/g)?.length,
      2
    );
    assert.match(content, /IPHONEOS_DEPLOYMENT_TARGET = 13\.0/);
  }
});
