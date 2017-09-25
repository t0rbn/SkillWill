import React from 'react'
import { Link } from 'react-router'
import './navigation.less'

const NavigationLogo = props => {
	return (
		<Link className="navigation__logo" to={props.target}>
			{props.children}
		</Link>
	)
}

export default NavigationLogo
