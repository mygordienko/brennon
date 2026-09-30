const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const TerserPlugin = require('terser-webpack-plugin'); 

module.exports = {
    entry: {
        app: './src/index.js',
    },
    output: {
        path: path.resolve(__dirname, '../resources/static/dist'),
        filename: '[name].bundle.js',
        clean: true,
    },
    module: {
        rules: [
            {
                test: /\.css$/i,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    'postcss-loader',
                ],
            },
        ],
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: '[name].bundle.css',
        }),
    ],
    // optimization: {
    //     minimize: true,
    //     minimizer: [
    //         new TerserPlugin({
    //             // Prevent Webpack from extracting comments into a separate .txt file
    //             extractComments: false, 
                
    //             terserOptions: {
    //             format: {
    //                 //  Completely strip comments from the final JS bundle
    //                 comments: false, 
    //             },
    //             },
    //         }),
    //     ], 
    // },

    optimization: {
        minimize: false,
        minimizer: [
        new TerserPlugin({
            extractComments: false,
        }),
        ],
    },
};
