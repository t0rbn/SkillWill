import React from 'react'
import config from '../../config.json'
import getStateObjectFromURL from '../../utils/getStateObjectFromURL'
import User from '../user/user'
import { browserHistory } from 'react-router'
import { connect } from 'react-redux'

class Results extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			lastSortedBy: 'fitness'
		}

		this.scrollToResults = this.scrollToResults.bind(this)
		this.sortResults = this.sortResults.bind(this)
	}

	scrollToResults() {
		const searchbarRect = document.querySelector('.searchbar').getBoundingClientRect()
		window.scrollBy({ top: `${searchbarRect.top - 10}`, behavior: "smooth" })
	}

	sortResults(criterion) {
		let sortedResults
		const { results } = this.props
		if (this.state.lastSortedBy === criterion) {
			sortedResults = results.reverse()
		} else if (criterion === 'fitness') {
			sortedResults = results.sort((a, b) => {
				return a[criterion] > b[criterion] ? -1 : 1
			})
		} else {
			sortedResults = results.sort((a, b) => {
				return a[criterion] < b[criterion] ? -1 : 1
			})
		}
		this.forceUpdate()

		this.setState({
			lastSortedBy: criterion,
			results: sortedResults
		})
	}

	render() {
		const { results, searchTerms } = this.props
		if (results && results.length > 0) {
			return (
				<div class="results-container">
					<a class="counter" onClick={this.scrollToResults}>
						<span>{results.length} Ergebnisse</span>
					</a>
					<ul class="results">
						<ul class="sort-buttons">
							<li class="sort-button-name" onClick={() => this.sortResults('name')}>Sort by Name</li>
							<li class="sort-button-location" onClick={() => this.sortResults('location')}>Sort by Location</li>
							<li class="sort-button-fitness" onClick={() => this.sortResults('fitness')}>Sort by Fitness</li>
						</ul>
						{results.map((data, i) => {
							return (
								<li class="result-item" key={i}>
									<User data={data} searchTerms={searchTerms} />
								</li>
							)
						})}
					</ul>
				</div>
			)
		}
		else {
			return (
				<div class="results-container" data-isEmptyLabel={this.props.noResultsLabel}></div>
			)
		}
	}
}

function mapStateToProps(state) {
	return {
		results: state.reducer.results,
		searchTerms: state.reducer.searchTerms
	}
}
export default connect(mapStateToProps)(Results)
