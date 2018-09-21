module.exports = {
    extends: [
        "airbnb"
    ],
    parser: "babel-eslint",
    plugins: [
        "react",
        "jsx-a11y",
        "import"
    ],
    rules: {
        "array-bracket-spacing": [ 2, "always" ],
        "arrow-parens": 0,
        "brace-style": [ 2, "stroustrup", { "allowSingleLine": true } ],
        "class-methods-use-this": [ 2, {
            "exceptMethods": [
                "render",
                "getInitialState",
                "getDefaultProps",
                "componentWillMount",
                "componentDidMount",
                "componentWillReceiveProps",
                "shouldComponentUpdate",
                "componentWillUpdate",
                "componentDidUpdate",
                "componentWillUnmount",
                "digestNewProps"
            ]
        } ],
        "comma-dangle": 0,
        "computed-property-spacing": [ 2, "always" ],
        "func-names": 0,
        "import/extensions": 0,
        "import/first": 0,
        "import/no-extraneous-dependencies": 1,
        "import/prefer-default-export": 0,
        "indent": [ 2, 4 ],
        "jsx-a11y/label-has-for": 0,
        "jsx-a11y/no-static-element-interactions": 0,
        "jsx-a11y/img-has-alt": 0,
        'max-len': ['error', 120, 4, {
            ignorePattern: "^ {40,}", // ignore deeply nested lines
            ignoreUrls: true,
            ignoreComments: true,
            ignoreStrings: true,
            ignoreTemplateLiterals: true,
        }],
        "new-cap": 0,
        "no-alert": 0,
        "no-multi-spaces": 0,
        "no-param-reassign": [ 2, { props: false } ],
        "no-plusplus": [ 2, { "allowForLoopAfterthoughts": true } ],
        "no-script-url": 0,
        "no-tabs": 0,
        "no-trailing-spaces": [ 2, { "skipBlankLines": true } ],
        "no-undef": 1,
        "no-underscore-dangle": 0,
        "no-use-before-define": [ 2, { "functions": false, "classes": false } ],
        "padded-blocks": 0,
        "prefer-arrow-callback": 0,
        "quotes": [ 2, "single", { allowTemplateLiterals: true } ],
        "react/forbid-prop-types": [ 0 ],
        "react/jsx-curly-spacing": [ 2, "always" ],
        "react/jsx-indent": [ 2, 4 ],
        "react/jsx-indent-props": [ 2, 4 ],
        "react/no-danger": 0,
        "react/prop-types": 0,
        "semi": [ 2, "never" ],
        "space-in-parens": [ 2, "always" ],

        "import/imports-first": 0,
        "react/jsx-no-bind": [ 0, { "allowArrowFunctions": true } ]
    },
    env: {
        "browser": true,
        "mocha": true
    },
    settings: {
        "import/resolver": "webpack"
    }
};
