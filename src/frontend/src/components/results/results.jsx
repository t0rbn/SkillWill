import React from 'react'
import config from '../../config.json'
import User from '../user/user'
import Dropdown from '../dropdown/dropdown.jsx'
import {connect} from 'react-redux'
import {setDirectionFilter, setSortFilter, stopAnimating,} from '../../actions'
import sortAndFilter from '../../utils/sortAndFilter.js'

class Results extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			lastSortedBy: 'fitnessScoreValue',
		}
	}

	componentDidMount() {
		this.node.addEventListener('animationend', () => {
			if (this.props.isSkillAnimated) {
				this.props.stopAnimating()
			}
		})
	}

	render() {
		const {
			directionFilter,
			lastSortedBy: { sortFilter },
			results: { searched, users },
			animated,
			setSortFilter,
			setDirectionFilter,
		} = this.props
		const { directionFilterOptions, sortFilterOptions } = config
		if (users && users.length > 0) {
			let sortedUserList = sortAndFilter(
				users,
				sortFilter,
				directionFilter,
			)
			return (
				<div ref={(ref) => { this.node = ref }} className={`results-container ${animated ? 'animateable' : ''}`}>
					<div className="counter">
						{sortedUserList.length} results, sorted
						<Dropdown
							onDropdownSelect={setDirectionFilter}
							dropdownLabel={directionFilter}
							options={directionFilterOptions}
						/>
						by
						<Dropdown
							onDropdownSelect={setSortFilter}
							dropdownLabel={sortFilter}
							options={sortFilterOptions}
						/>
					</div>
					<div className="results-legend-wrapper">
						<div className="results-legend container">
							<div className="results-legend-item name">Name</div>
							<div className="results-legend-item skills">
								<div className="skill-label">Skill</div>
								<div className="skill-level">Skill level</div>
								<div className="will-level">Will level</div>
							</div>
						</div>
					</div>
					<div className="results">
						<ul className="results-list container">
							{sortedUserList.map(user => {
								return (
									<li className="result-item" key={user.id}>
										<User user={user} searchTerms={searched} />
									</li>
								)
							})}
						</ul>
					</div>
				</div>
			)
		} else if (users) {
			return (
				<div
					ref={(ref) => { this.node = ref }}
					className="no-results-container"
					data-isEmptyLabel={this.props.noResultsLabel}>
					<div className="container">
						No results found :(
					</div>
				</div>
			)
		} else {
			return null;
		}
	}
}

function mapStateToProps(state) {
	return {
		results: state.results,
		searchTerms: state.searchTerms,
		lastSortedBy: state.lastSortedBy,
		directionFilter: state.directionFilter,
		isSkillAnimated: state.isSkillAnimated,
	}
}
export default connect(mapStateToProps, {
	setSortFilter,
	setDirectionFilter,
	stopAnimating,
})(Results)
