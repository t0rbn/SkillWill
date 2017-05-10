import {
	combineReducers
} from 'redux'
import {
	FETCH_RESULTS,
	FETCH_SKILL,
	SAVE_SEARCHTERMS_TO_STORE,
	GET_PROFILE_DATA
} from '../actions'

function saveSearchTermsToStore(state = [], action) {
	switch (action.type) {
		case SAVE_SEARCHTERMS_TO_STORE:
			return [...action.searchTerms]
		default:
			return state
	}
}

function fetchResultsBySearchTerms(state = [], action) {
	switch (action.type) {
		case FETCH_RESULTS:
			return {state, results: action.payload.results, searched: action.payload.searched}
		default:
			return state
	}
}

function fetchSkillBySearchTerm(state = [], action) {
	switch (action.type) {
		case FETCH_SKILL:
			return Object.assign({}, ...action.payload)
		default:
			return state
	}
}

function getUserProfileData(state = [], action) {
	switch (action.type) {
		case GET_PROFILE_DATA:
			return Object.assign({}, state, action.payload)
		default:
			return state
	}
}

const rootReducer = combineReducers({
	searchTerms: saveSearchTermsToStore,
	results: fetchResultsBySearchTerms,
	user: getUserProfileData,
	skill: fetchSkillBySearchTerm
})

export default rootReducer