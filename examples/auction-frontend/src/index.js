/* eslint-disable react/jsx-filename-extension */
import React from 'react'
import ReactDOM from 'react-dom'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/css/bootstrap-theme.css'
import './css/index.css'
import App from './js/App.jsx'
import registerServiceWorker from './js/registerServiceWorker'

ReactDOM.render( <App />, document.getElementById( 'root' ) )
registerServiceWorker()
