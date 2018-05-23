const webpack = require('webpack')
const path = require('path')
const loaders = require('./webpack.loaders')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const WebpackCleanupPlugin = require('webpack-cleanup-plugin')
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')

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
		publicPath: '/',
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
	optimization: {
		minimizer: [
			// we specify a custom UglifyJsPlugin here to get source maps in production
			new UglifyJsPlugin({
				cache: true,
				parallel: true,
				uglifyOptions: {
					compress: false,
					ecma: 6,
					mangle: true
				},
				sourceMap: true
			})
		]
	}
}
