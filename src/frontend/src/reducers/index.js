import { combineReducers } from 'redux'
import {
	FETCH_RESULTS,
	FETCH_SKILLS,
	SAVE_SEARCHTERMS_TO_STORE
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

function getSearchTerms(state = [], action){
	switch (action.type) {
		case SAVE_SEARCHTERMS_TO_STORE:
			return [...action.searchTerms]
		default:
			return state
	}
}

const rootReducer = combineReducers({
  results: fetchResultsBySearchTerms,
	searchTerms: getSearchTerms
	// skills: fetchSkillsBySearchTerms
})

export default rootReducer
