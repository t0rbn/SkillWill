import React from 'react'
import { Router, Route, Link } from 'react-router'
import RangeSlider from '../range-slider/range-slider.jsx'
import Config from '../../config.json'

export default class Editor extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillLevel: this.props.skillLvl,
			willLevel: this.props.willLvl
		}
		this.handleSliderChange = this.handleSliderChange.bind(this)
		this.handleAccept = this.handleAccept.bind(this)
	}

	handleSliderChange(val, type) {
		if (type === 'skill') {
			this.setState({
				skillLevel: val
			})
		} else {
			this.setState({
				willLevel: val
			})
		}
	}

	handleAccept() {
		const { skillName, isMentor } = this.props
		const { skillLevel, willLevel } = this.state
		this.props.handleAccept(skillName, skillLevel, willLevel, isMentor)
		this.props.handleClose()
	}

	render() {
		const { skillLevel, willLevel } = this.state
		return (
			<div className="editor">
				<div className="action-buttons">
					<a className="check" onClick={this.handleAccept}></a>
					<a className="cancel" onClick={this.props.handleClose}></a>
				</div>
				<div className="slider-container">
					<p className="slider-description">Your skill level</p>
					<RangeSlider
						onSlide={this.handleSliderChange}
						type="skill"
						value={skillLevel}
						legend={Config.skillLegend} />
					<p className="slider-description">Your will level</p>
					<RangeSlider
						onSlide={this.handleSliderChange}
						type="will"
						value={willLevel}
						legend={Config.willLegend} />
				</div>
			</div>
		)
	}
}
