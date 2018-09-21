import axios from 'axios'

const ajax = axios.create()

ajax.interceptors.response.use(
    response => response,
    error => {
        if ( !error.response ) {
            return Promise.reject( {} )
        }
        return Promise.reject( error.response )
    }
)

export default ajax
