const assert = require('node:assert/strict');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const test = require('node:test');

const root = path.resolve(__dirname, '..');

test('Objective-C payment helper routes callbacks and builds result payloads', () => {
  const executable = path.join(os.tmpdir(), `ios-paysdk-behavior-${process.pid}`);
  const compile = spawnSync('xcrun', [
    'clang',
    '-fobjc-arc',
    '-Wall',
    '-Wextra',
    '-Werror',
    '-framework',
    'Foundation',
    '-I',
    path.join(root, 'src/ios'),
    path.join(root, 'src/ios/CDVWechatPayCoordinator.m'),
    path.join(__dirname, 'ios-paysdk-behavior.m'),
    '-o',
    executable,
  ], { encoding: 'utf8' });

  assert.equal(compile.status, 0, compile.stderr || compile.stdout);

  const run = spawnSync(executable, [], { encoding: 'utf8' });
  assert.equal(run.status, 0, run.stderr || run.stdout);
});
