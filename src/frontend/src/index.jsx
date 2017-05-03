import React from 'react'
import { render } from 'react-dom'
import { AppContainer } from 'react-hot-loader'
import { Router, Route, browserHistory, IndexRoute } from 'react-router'
import { createStore, combineReducers, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import { syncHistoryWithStore, routerReducer } from 'react-router-redux'
import thunkMiddleware from 'redux-thunk'
import ReduxPromise from 'redux-promise'

import reducer from './reducers'

import App from './app.jsx'
import UserSearch from './components/search/user-search.jsx'
import Results from './components/results/results.jsx'
import SkillSearch from './components/search/skill-search.jsx'
import Layer from './components/layer/layer.jsx'
import MyProfile from './components/profile/my-profile.jsx'
import OthersProfile from './components/profile/others-profile.jsx'
import Login from './components/login/login.jsx'
import Logout from './components/logout/logout.jsx'


const store = createStore(
	combineReducers({
		reducer,
		routing: routerReducer
	}),
	applyMiddleware(
		ReduxPromise
	)
)

const history = syncHistoryWithStore(browserHistory, store)

render(
	<Provider store={store}>
		<Router history={history}>
			<Route path="/" component={App}>
				<Route path="search" name="search" component={Results} />
				<Route path="profile" component={Layer}>
					<Route path=":id" component={OthersProfile} />
				</Route>
				<Route path="my-profile" component={Layer}>
					<Route path="login" component={Login} />
					<Route path="logout" component={Logout} />
					<Route path=":id" component={MyProfile}>
						<Route path="add-skill" component={Results} />
					</Route>
				</Route>
			</Route>
		</Router>
	</Provider>, document.querySelector("#app")
)
