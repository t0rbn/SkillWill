import { combineReducers } from 'redux'
import {
	FETCH_RESULTS,
	FETCH_SKILLS,
	KEEP_SEARCHTERMS
} from '../actions'

function fetchResultsBySearchTerms(state = [], action) {
	switch (action.type) {
		case FETCH_RESULTS:
		case FETCH_SKILLS:
			return [...action.payload]
		default:
			return state
	}
}

const rootReducer = combineReducers({
  results: fetchResultsBySearchTerms
})

export default rootReducer
