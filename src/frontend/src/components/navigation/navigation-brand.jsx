import React from 'react'
import { Link } from 'react-router'
import './navigation.less'

const NavigationBrand = props => (
	<Link className="navigation__brand" to={props.target} />
)

export default NavigationBrand
