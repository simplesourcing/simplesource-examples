import React from 'react'
import { BrowserRouter as Router } from 'react-router-dom'
import Routes from './Routes.jsx'
import '../css/index.css'

const App = () => ( <Router>
    <div className="container">
        <Routes />
    </div>
</Router> )

export default App
