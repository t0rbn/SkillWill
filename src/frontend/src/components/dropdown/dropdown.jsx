import React from 'react'

export default class Dropdown extends React.Component {
	constructor(props) {
		super(props)

		this.handleDropdownChange = this.handleDropdownChange.bind(this)
		this.setDropdownValue = this.setDropdownValue.bind(this)
		this.setDropdownLabel = this.setDropdownLabel.bind(this)
	}

	handleDropdownChange(e) {
		const location = e.target.value
		this.props.onDropdownSelect(location)
	}

	setDropdownValue() {
		const { dropdownLabel } = this.props
		if (dropdownLabel !== 'all') {
			return dropdownLabel
		} else {
			return 'all'
		}
	}
	setDropdownLabel() {
		const { dropdownLabel } = this.props
		if (dropdownLabel !== 'all') {
			return dropdownLabel
		} else {
			return 'Alle Standorte'
		}
	}

	render() {
		return (
			<div className="dropdown">
				<span className="dropdown-label">{this.setDropdownLabel()}</span>
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


