import React from 'react'

export default class Dropdown extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			dropdownLabel: this.props.dropdownLabel
		}
		this.handleDropdownChange = this.handleDropdownChange.bind(this)
		this.setDropdownValue = this.setDropdownValue.bind(this)
		this.setDropdownLabel = this.setDropdownLabel.bind(this)
	}

	handleDropdownChange(e) {
		const location = e.target.value
		this.props.onDropdownSelect(location)
	}

	setDropdownValue() {
		if (this.props.dropdownLabel !== 'all') {
			return this.props.dropdownLabel
		} else {
			return 'all'
		}
	}
	setDropdownLabel() {
		if (this.props.dropdownLabel !== 'all') {
			return this.props.dropdownLabel
		} else {
			return 'Alle Standorte'
		}
	}

	render() {
		return (
			<div class="dropdown">
				<span class="dropdown-label">{this.setDropdownLabel()}</span>
				<select onChange={this.handleDropdownChange}
					value={this.setDropdownValue()}>
					<option value="all">Alle Standorte</option>
					<option value="Hamburg">Hamburg</option>
					<option value="Frankfurt">Frankfurt</option>
					<option value="München">München</option>
				</select>
			</div>
		)
	}
}


