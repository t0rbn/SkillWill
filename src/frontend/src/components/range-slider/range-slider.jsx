import React from 'react'
import { Router, Route, Link } from 'react-router'

export default class RangeSlider extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            progress: "lvl" + this.props.defaultValue
        }
        this.handleChange = this.handleChange.bind(this)
    }

    handleChange(e) {
        e.preventDefault()
        let val =  e.target.value
        this.props.onSlide(val, this.props.type)
    }

    render() {
        return(
            <div className="range-slider">
                <div className={`level-bar ${this.state.progress}`}></div>
                <span className="step lvl1"></span>
                <span className="step lvl2"></span>
                <span className="step lvl3"></span>
                <input type='range' min='0' max='3' value={this.props.defaultValue} defaultValue={this.props.defaultValue} onChange={this.handleChange} />
                <div className="legend">
                    {this.props.legend.map( (data, i) => {
                        return (
                            <div className={`legend-level-${i}`} key="{i}">
															<span className="legend-label">{data}</span>
														</div>
                        )
                    })}
                </div>
            </div>
        )
    }
}
