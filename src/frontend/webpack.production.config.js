const webpack = require('webpack')
const path = require('path')
const loaders = require('./webpack.loaders')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const WebpackCleanupPlugin = require('webpack-cleanup-plugin')
const ExtractTextPlugin = require('extract-text-webpack-plugin')

loaders.push({
	test: /\.less$/,
	loader: ExtractTextPlugin.extract({
		use: [
			{
				loader: 'css-loader',
			},
			{
				loader: 'less-loader',
			},
		],
		fallback: 'style-loader',
	}),
	exclude: [/(node_modules|public\/)/],
})

module.exports = {
	entry: ['./src/index.jsx', './src/styles.less'],
	output: {
		publicPath: './',
		path: path.join(__dirname, 'public'),
		filename: '[chunkhash].js',
	},
	resolve: {
		extensions: ['.js', '.jsx'],
	},
	module: {
		loaders,
	},
	plugins: [
		new WebpackCleanupPlugin(),
		new webpack.DefinePlugin({
			'process.env': {
				NODE_ENV: JSON.stringify('production'),
				API_SERVER: JSON.stringify(''),
			},
		}),
		new webpack.optimize.UglifyJsPlugin({
			compress: {
				warnings: false,
				screw_ie8: true,
				drop_console: true,
				drop_debugger: true,
			},
		}),
		new webpack.optimize.OccurrenceOrderPlugin(),
		new ExtractTextPlugin({
			filename: 'style.css',
			allChunks: true,
		}),
		new HtmlWebpackPlugin({
			template: './src/template.html',
			files: {
				css: ['style.css'],
				js: ['bundle.js'],
			},
		}),
	],
}
