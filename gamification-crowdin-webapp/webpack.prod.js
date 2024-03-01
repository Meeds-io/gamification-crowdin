const path = require('path');
const { merge } = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

const config = merge(webpackCommonConfig, {
  mode: 'production',
  entry: {
    engagementCenterExtensions: './src/main/webapp/vue-app/engagementCenterExtensions/extensions.js',
    connectorExtensions: './src/main/webapp/vue-app/connectorExtensions/extensions.js',
    crowdinUserConnectorExtension: './src/main/webapp/vue-app/crowdinUserConnectorExtension/extension.js',
    crowdinAdminConnectorExtension: './src/main/webapp/vue-app/crowdinAdminConnectorExtension/extension.js'
  },
  output: {
    path: path.join(__dirname, 'target/gamification-crowdin/'),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  }
});

module.exports = config;
