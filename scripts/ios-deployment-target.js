const fs = require('node:fs');
const path = require('node:path');

const DEPLOYMENT_TARGET = '12.0';

function updateProject(file) {
  const original = fs.readFileSync(file, 'utf8');
  const updated = original.replace(
    /IPHONEOS_DEPLOYMENT_TARGET = ([^;]+);/g,
    (setting, version) =>
      Number.parseFloat(version) < Number.parseFloat(DEPLOYMENT_TARGET)
        ? `IPHONEOS_DEPLOYMENT_TARGET = ${DEPLOYMENT_TARGET};`
        : setting
  );
  fs.writeFileSync(file, updated);
}

module.exports = function enforceDeploymentTarget(context) {
  const platforms = context.opts.platforms || [];
  if (!platforms.includes('ios')) {
    return;
  }

  const iosRoot = path.join(context.opts.projectRoot, 'platforms', 'ios');
  const projectFiles = fs
    .readdirSync(iosRoot, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && entry.name.endsWith('.xcodeproj'))
    .map((entry) => path.join(iosRoot, entry.name, 'project.pbxproj'));
  projectFiles.push(
    path.join(iosRoot, 'CordovaLib', 'CordovaLib.xcodeproj', 'project.pbxproj')
  );

  for (const file of projectFiles) {
    updateProject(file);
  }
};
