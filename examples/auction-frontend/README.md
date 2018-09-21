#### Overview

This is a sample React frontend application for the auction example Simple Sourcing application.

It allows a user to:
* Create accounts
* View account details, and corresponding transactions
* Add funds to account
* Reserve funds against account
* Confirm or cancel reservations

#### Running in docker

To run the full stack (almost) :

1. Change to the `examples/auction-frontend` folder
1. Run `docker-compose up`
1. Go to `http://localhost:3000` in a browser window

#### Manual execution (outside of docker)

It can also be run and executed outside of docker.

1. Install Node.js (this should install npm as well)
1. Install yarn: `npm install -g yarn`
1. Run `yarn install`
1. Run `yarn start`
1. Go to `http://localhost:3000` in a browser window

Note that the backend stack must be running for the application to work.

Follow the quick start instructions for the auction backend service in `../auction`
