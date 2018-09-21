const hostname = process.env.REACT_APP_HOST_NAME || 'localhost'

export const API_BASE_URL = `http://${hostname}:8080/auction-example`
export const API_BASE_URL_PROJECTION = `http://${hostname}:8080/auction-example/projections`
