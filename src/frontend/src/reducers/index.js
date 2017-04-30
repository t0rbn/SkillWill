import { combineReducers } from 'redux'
import {
	FETCH_RESULTS,
	FETCH_SKILLS,
	KEEP_SEARCHTERMS
} from '../actions'

function resultsBySearchTerms(state = [], action) {
	switch (action.type) {
		case FETCH_RESULTS:
		case FETCH_SKILLS:
			return [...action.payload]
		default:
			return state
	}
}
function keepSearchTerms(state = [], action){
	console.log('reduce', action.searchTerms)
	switch (action.type) {
		case KEEP_SEARCHTERMS:
			return [...state, ...action.searchTerms]
		default:
			return state
	}
}

const rootReducer = combineReducers({
  results: resultsBySearchTerms,
	keepSearchTerms
})

export default rootReducer
