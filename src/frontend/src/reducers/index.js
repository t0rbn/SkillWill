import { combineReducers } from 'redux'
import {
	ADD_TO_URL,
	REQUEST_RESULTS,
	RECEIVE_RESULTS,
	SEARCH_TERMS
} from '../actions'

// export default function URL(state = initialState, action) {
// 	switch (action.type) {
// 		case 'ADD_TO_URL':
// 			const
// 	}
// }

function searchTerms(state = {}, action) {
	switch (action.type) {
		case SEARCH_TERMS:
			return action.searchTerms
		default:
			return state
	}
}

function results(state = {
	isFetching: false,
	results: []
}, action) {
	switch (action.type) {
		case REQUEST_RESULTS:
			console.log('REQUEST_RESULTS')
			return Object.assign({}, state, {isFetching: true})
		case RECEIVE_RESULTS:
			console.log('RECEIVE_RESULTS')
			return Object.assign({}, state, {
				isFetching: false,
				results: action.results
		})
		default:
			return state
	}
}

function resultsBySearchTerms(state={}, action) {
	switch (action.type) {
		case REQUEST_RESULTS:
		case RECEIVE_RESULTS:
			return Object.assign({}, state, {
				[action.searchTerms]: results(state[action.searchTerms], action)
			})
			default:
				return state
	}
}

const rootReducer = combineReducers({
  searchTerms,
  resultsBySearchTerms
})

export default rootReducer
