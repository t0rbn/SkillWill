import React from 'react'
import { Router, Route, Link } from 'react-router'

export default class RangeSlider extends React.Component {
	constructor(props) {
		super(props)
		this.handleChange = this.handleChange.bind(this)
	}

	handleChange(e) {
		const { type } = this.props
		e.preventDefault()
		let value = e.target.value
		this.props.onSlide(value, type)
	}

	render() {
		const { value, legend } = this.props
		return (
			<div className="range-slider">
				<div className={`level-bar lvl${value}`}></div>
				<span className="step lvl1"></span>
				<span className="step lvl2"></span>
				<span className="step lvl3"></span>
				<input type='range' min='0' max='3' value={`${value}`} onChange={this.handleChange} />
				<div className="legend">
					{legend.map((data, i) => {
						return (
							<div className={`legend-level-${i}`} key={data}>
								<span className="legend-label">{data}</span>
							</div>
						)
					})}
				</div>
			</div>
		)
	}
}
