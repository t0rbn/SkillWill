import React from 'react'
import './ticket-notice.less'

const createTicketUrl =
	'https://jira.sinnerschrader.com/secure/CreateIssueDetails!init.jspa?pid=15352&issuetype=3&priority=4'
const cssClass = 'ticket-notice'

const TicketNotice = ({ title, subtitle }) => (
	<div className={cssClass}>
		<h2 className={`${cssClass}-title`}>{title}</h2>
		<h3 className={`${cssClass}-subtitle`}>
			<a href={createTicketUrl}>{subtitle}</a>
		</h3>
	</div>
)

export default TicketNotice
