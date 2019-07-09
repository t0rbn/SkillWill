import React from 'react'
import Navigation from '../navigation/navigation'
import NavigationItem from '../navigation/navigation-item'
import NavigationLink from '../navigation/navigation-link'
import NavigationList from '../navigation/navigation-list'
import NavigationListItem from '../navigation/navigation-list-item'
import Icon from '../icon/icon.jsx'

export default class Header extends React.Component {

	render() {
		return (
			<header className="header">
				<Navigation>
					<NavigationItem>
						<NavigationLink target={'/'}>
							<Icon name="mm-logo" width={56} height={30} />
						</NavigationLink>
					</NavigationItem>
					<NavigationItem>
						<NavigationList>
							<NavigationListItem
								target={'/my-profile'}>
								<Icon name="user" width={20} height={20} />
							</NavigationListItem>
						</NavigationList>
					</NavigationItem>
				</Navigation>
			</header>
		)
	}

}
