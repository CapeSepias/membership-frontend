package model

import configuration.{Config, Email}
import controllers.routes
import utils.StringUtils.slugify
import play.twirl.api.Html

object Faq {
  case class Section(title: String, questions:List[Item]) {
    def id = "s_" concat title.filter(_.isLetter)
  }
  case class Item(question: String, answer: Html) {
    def id = "q_" concat question.filter(_.isLetter)
  }

  val staff = List(
    Item("I already have a profile set up on theguardian.com, can I use that?",
       Html(s"Yes, but only if it's set up with your work email address ending @theguardian.com (@guardian.co.uk will work too). <a href='${Config.idWebAppUrl + "/account/edit"}'>Check my profile on the guardian.com</a>")
    ),
    Item("Can I use social sign in/registration?",
      Html("No, you must have a Guardian account using your work email address.")
    ),
    Item("I've already joined Guardian Members as a friend, can I upgrade?",
      Html("No, sorry you can't upgrade, you need to cancel your friend membership first.")
    ),
    Item("How do I cancel my friend membership?",
      Html(s"<ol><li>Go to <a href='${Config.membershipUrl + "/tier/cancel"}'>${Config.membershipUrl + "/tier/cancel"}</a></li><li>Scroll to the bottom and click 'Cancel membership'.</li></ol>")
    ),
    Item("How do I change the email address saved in my profile on theguardian.com?",
      Html(s"<ol><li>Go to <a href='${Config.idWebAppUrl + "/account/edit"}'>${Config.idWebAppUrl + "/account/edit"}</a></li><li>Change the email address to your work email address</li><li>Scroll to the bottom and click 'Save Changes'</li></ol>")
    ),
    Item("How long will staff membership last?",
      Html("Your Guardian Staff Membership will last for a long as you are a permanent member of staff")
    ),
    Item("I've got an additional question that's not listed here",
      Html(s"Please email <a href='mailto:${Email.staffMembership}'>${Email.staffMembership}</a> with your question, use Guardian Staff Partners as the Subject. We will do our best to get back to you within 24 hours.")
    )
  )

  val help = List(
    Section("Guardian Membership: the basics", List(
      Item("What is membership?",
        Html("Membership is an opportunity for readers to offer financial support to the Guardian’s journalism, and for the Guardian to deepen its relationship with its readers. Quality, independent, investigative journalism is expensive and takes time to produce. The decline in print advertising sales, along with the rise of digital advertising spend going to social media and tech giants, means we must look for alternative means to fund our journalism. Lots of people around the world read, watch and listen to the Guardian’s journalism but many do not pay for it. Rather than setting up a paywall, we are asking those who can afford it to make their contribution to our fearless, independent journalism. Become a Member if you share our belief that the open exchange of information, ideas and opinions can help to change the world for the better.")),
      Item("What do I get if I join and how much does it cost?", Html("<p>Supporters pay £5/US$6.99/AU$10 per month or £49/US$69/AU$100 per year – as well as supporting the Guardian’s journalism, benefits include an ad-free experience of the Guardian app, a welcome gift, an exclusive monthly podcast (from January 2017) and emails from our journalists to keep you up to date with the latest from the newsroom, including ‘behind the scenes’ articles commissioned specially for Members.</p><p>Alternatively you can become a partner for £15 a month (£149 per year), and get all of the above as well as free books published by the Guardian and Faber, and tickets to the Guardian’s live events.</p><p>Or you can become a patron from £1,200 per year, which also includes exclusive invitations to special events and moments with the Guardian.</p>")),
      Item("What is the difference between a Supporter and a Partner?", Html("You can read a full description of each membership tier <a href=\"https://membership.theguardian.com/choose-tier\">here.</a>")),
      Item("What if I just want to give money to the Guardian?", Html("There is also the option to make a <a href=\"https://contribute.theguardian.com/?INTCMP=membership_faq\"> contribution</a> if this is more suitable. All funds raised from readers will go directly into supporting our journalism. We just ask for your name and email address in order for you to do this (we need this in order to process refunds and receipts, and in case of any transactional issues). ")),
      Item("Why don't you just have a paywall?", Html("<p>We are currently running a series of tests asking people who access theguardian.com if they would like to make a financial contribution, either through becoming a Guardian  Supporter or through a one-off contribution. We hope that this will help sustain our journalism and enable us to uncover the stories that need to be told while – crucially – Guardian journalism remains open to all.</p><p>Of course a paywall is something we have looked at and we would never rule it out but putting one up now would get in the way of growing and deepening the relationships that are critical for our future.</p>")),
      Item("Couldn’t the Guardian just get its owner to provide more money?", Html("The Guardian does not have a wealthy owner pulling the strings. No shareholders, advertisers or billionaire owners can edit our editor. The Scott Trust forms part of a unique ownership structure for the Guardian. Created in 1936, the Trust was set up to secure the financial and editorial independence of the Guardian in perpetuity and to safeguard its journalistic freedom and liberal values, free from commercial or political interference. It reinvests revenue into our journalism, as opposed to into shareholders' pockets. More information can be found <a href=\"https://www.theguardian.com/membership/video/2016/nov/11/who-owns-the-guardian-and-how-is-it-funded\">here</a> and <a href=\"https://www.theguardian.com/the-scott-trust\">here</a>.")),
      Item(" Does membership provide an ad-free experience?", Html("As part of your membership, you qualify for free access to the premium tier of the award-winning Guardian news app, which has no adverts, allows you to follow your favourite journalists, sign up for alerts to key topics, series and sports events, and gives you access to our crosswords. You can download the app by visiting <a href=\"https://theguardian.com/guardianapp\">theguardian.com/guardianapp</a>, and then you simply need to sign in with your Guardian Members login details.  If you encounter any difficulties accessing the premium tier, please ensure you have the latest version of the app downloaded. Once in the app you can choose between the UK, US, Australia or International edition. At the moment, we don’t offer an ad-free experience on other platforms, but this is something we are exploring.")),
      Item("I’m already a Member, but I’m still seeing membership related ads and promotions.", Html("You can avoid seeing membership ads if you <a href=\"https://profile.theguardian.com/signin\">sign in</a> at the top left hand corner of <a href='https://theguardian.com'>theguardian.com</a> and on the Guardian app, so we know you are a Member. If you’ve forgotten or need to retrieve your password you can reset it <a href='https://profile.theguardian.com/reset'>here</a>.")),
      Item("What happens if I don't want to receive membership emails?", Html("If you no longer want to receive our email updates for Members, click the unsubscribe link at the bottom of the email. We have also created a <a href='https://contribute.theguardian.com/?INTCMP=membership_faq'>one-off contributions</a> page specifically for loyal readers who want to make a financial contribution but don't want to receive regular communications.")),
      Item("I want to update the personal details (phone, email or address) connected to my membership account", Html("You can update any of your personal details by simply <a href='https://profile.theguardian.com/signin'>signing in</a> to your membership account and clicking <a href='https://profile.theguardian.com/account/edit'>edit profile</a> to update your account details. Any changes will be automatically updated in our system.")),
      Item("My membership welcome gift hasn’t arrived yet", Html("Your welcome gift should be delivered to you three weeks after joining us. If you have not received it by this time, please email the membership customer support team at <a href='mailto:membershipsupport@theguardian.com'>membershipsupport@theguardian.com</a> and quote your Membership ID number")),
      Item("Is it possible to make a contribution or sign up using PayPal?", Html("We are in the process of setting up PayPal for membership. We currently have PayPal available for <a href='https://contribute.theguardian.com/?INTCMP=membership_faq'>contributions</a>.")),
      Item("Gift Membership",Html("Unfortunately, it's not currently possible to purchase a gift membership on behalf of someone else as we require all new members to join using their own details, including payment information. However, gift membership is something that we would like to offer in the future.")),
      Item("Can I pay by Direct Debit?",Html("There is currently no Direct Debit payment option for membership. We accept Visa, MasterCard and American Express.")),
      Item("Why can't I choose which currency to pay in?",Html("Our technical systems only allow us to take payments based on the currency of the country where your billing account is registered. ")),
      Item("Can I send a cheque?", Html("<p>Yes, if you want to make a <a href='https://contribute.theguardian.com/?INTCMP=membership_faq'>contribution</a>, but not if you want to become a Member. </p><p>Cheques for one-off contributions towards the Guardian’s journalism can be sent to one of the following addresses:</p><p>The US:<br />Payable to: Guardian News & Media LLC<br />Address: FAO: Finance Department, The Guardian, 61 Broadway, New York, NY, 10006</p><p>Australia:<br />Payable to: GNM Australia Pty Ltd<br />Address: FAO Finance Department at Guardian Australia, Level 2, 19 Foster Street, Surry Hills, NSW 2010</p><p>UK and Rest of World:<br />Payable to: Guardian News & Media<br />Address: FAO Cash Management, Finance Department. Address: Kings Place, 90 York Way, London, N1 9GU.</p>")),
      Item("Can I get involved in Guardian journalism?", Html("Details on contributing to the Guardian can be found <a href='https://www.theguardian.com/info/1999/nov/22/contributors-guide-and-contacts'>here</a>. Readers can also get involved via <a href='https://witness.theguardian.com/'>GuardianWitness</a>. And we regularly ask Members for their views and opinions on our journalism and how we cover ongoing stories and issues.")),
      Item("I can't afford to pay you anything yet, but would like to offer my support.", Html("You can join Guardian Membership as a friend for free to receive regular updates. ")),
      Item("How does membership fit with subscriptions? ", Html("We are currently reviewing how membership is priced, what it includes/involves, and how it fits with the various print and digital content packages that we offer readers, including subscriptions. ")),
      Item("I am under 18 - can I still be a Member?", Html("No. You have to be 18 or over to join.")),
      Item("How do I know you are protecting my personal information?", Html("We will never use your information for any purpose – marketing or otherwise – without your permission. You can read more about our approach to data security in our <a href='http://www.theguardian.com/help/privacy-policy'>privacy policy</a>.")),
      Item(" Can I become a Member by phone?", Html("Unfortunately it isn't currently possible to become a Member over the phone. To join you need to set up a password and we can't currently safely do that over the phone, according to our data protection regulations. This is something we may try to remedy in the future.")),
      Item("I don’t live in the UK – can I still be a Member?", Html("Yes, Guardian Membership is open to anyone living anywhere in the world. ")),
      Item("What happens if I want to change my membership tier?", Html("You can change your membership tier online anytime. Just go to your <a href='https://profile.theguardian.com/membership/edit'>Profile page</a> on the membership site and follow the instructions.")),
      Item("What happens if I want to cancel my membership?", Html("You can cancel your membership by following the instructions on <a href='https://membership.theguardian.com/tier/cancel'>this page</a>. Your membership will run until the end of your current payment term and then renew on an ongoing basis unless you choose to cancel.")),
      Item("What are the terms and conditions of membership?", Html("Read the full set of terms and conditions <a href='https://www.theguardian.com/info/2014/sep/09/guardian-membership-terms-and-conditions'>here</a>.")),
      Item("My question hasn’t been answered here", Html(
        """
          <p>Please email <a href='mailto:membershipsupport@theguardian.com'>membershipsupport@theguardian.com</a> with your question. We will do our best to get back to you within 24 hours. Alternatively, you can call the Guardian Members customer services team in the UK on 0330 333 6898 from 8am to 8pm Monday to Friday and 8am to 6pm at weekends.</p>
          <p>Readers in Australia can email <a href='mailto:australia.membership@theguardian.com'>australia.membership@theguardian.com</a> or call the Guardian Australia office on 1800 773 766, lines are open 9am-5:00pm Monday - Friday (AEDT).</p>
          <p>Readers in the US can email <a href='mailto:membershipsupport@theguardian.com'>membershipsupport@theguardian.com</a></p>
          <p>You can also leave feedback via <a href='https://membership.theguardian.com/feedback'>this form</a>. </p>"""))
    )),
    Section("Guardian Live (Events)",List(
      Item("What is Guardian Live?",Html("Guardian Live is a rolling events programme of discussions, debates, interviews and festivals that transform our journalism in print and online into live experiences. Recent examples include cooking lessons from Nigella Lawson and Jeanette Winterson, guided tours of the Guardian archives and printing presses, and talks from Johnny Marr, Bryan Cranston and Alan Bennett. Tickets can only be booked by Guardian Members.")),
      Item("Are all of your events in London?",Html("<p>We now run around 250 Guardian Live events a year in the UK. There are also a number of events held for members in the US and Australia. Around 35% of Guardian Live events happen outside London; that figure is not higher primarily because the additional costs involved in regional events (travel/accommodation/venue hire) can mean it’s hard to make the figures work. However, in addition, our regular Local programme takes place all over the country. Visit <a href='http://membership.theguardian.com/events'>membership.theguardian.com/events</a> to see the calendar of current events.</p><p>We plan to continue building our programme outside London and are working on a number of partnerships with venues and organisations. </p><p>We also have plans which will allow us to offer more to Members who can't make it to events. We try to live stream bigger events, our new monthly Members’ podcast will bring our lively newsroom events directly to Members and also feature their questions and viewpoints, and we continue to run live Q&As with our journalists on theguardian.com; on the day of the EU referendum result, editor-in-chief Katharine Viner and editor-at-large Gary Younge answered your questions.</p>")),
      Item("Can I buy a ticket to a Guardian Live event without becoming a Member?",Html("You can only buy a ticket to a Guardian Live event if you are a member. Access to these events is one of the benefits we offer Members in return for their support. ")),//TODO: should this refer to friend
      Item("Can I buy a ticket to an event without signing in?",Html("You are required to sign in before buying tickets for Guardian Live events.")),
      Item(" How many Guardian Live tickets can I buy for each event?",Html("You can buy as many tickets as you want. Partners are entitled to two tickets at a discount. After you’ve bought your discounted tickets you can come back and buy as many as you want at full price. We are working to improve this so you can buy all your tickets in one go.")),
      Item("If I choose the Tickets benefit, how do I redeem my tickets?",Html("If you have selected ‘tickets’ as your Partner benefit, your first six tickets to Guardian Live are available to book immediately. You may use one per event, and this will automatically be applied when you visit the booking page. Please note the six tickets are not valid for Guardian Local or Guardian Masterclasses.")),
      Item("If I choose the Book benefit, which books will I receive and when?",Html("If you have selected ‘books’ as your Partner benefit, you will receive four Guardian books specially selected for Members from Guardian just-published titles. You will receive your first book within 30 days of joining and one book every 3 months thereafter.")),
      Item("Can I change from the Tickets Benefit to the Book Benefit?",Html("Yes, you can choose to move from one benefit type to another, but only if you have not yet redeemed a ticket, or received a book in the applicable membership year. To do so, contact membership support by either emailing <a href='mailto:membershipsupport@theguardian.com'>membershipsupport@theguardian.com</a> or calling 0330 333 6898.")),
      Item("How will I know what Guardian Live events are coming up?",Html("Our events listing page is updated regularly and when you join as a Member you will receive a weekly email newsletter containing information about upcoming events.")),
      Item("How can I find out about disabled/wheelchair access for the venue?",Html("We only run events at venues that are accessible by wheelchair. You can tell us that you require accessibility information on the ticket booking form and we will contact you to go over the access route before the event."))
    )),
    Section("Guardian Masterclasses",List(
      Item("What are Guardian Masterclasses?",Html("Guardian Masterclasses match our readers’ interests with a wide range of courses and workshops, harnessing the expertise and specialisms of award-winning Guardian professionals and leading figures from the creative and digital industries. Recent examples include data visualisation workshops, a lyric-writing lesson from Chris Difford, and a masterclass in becoming a successful journalist from some of our award-winning writers. More frequently asked questions are available <a href='http://www.theguardian.com/guardian-masterclasses/faqs'>here</a>.")),
      Item("Can I book a Guardian Masterclass course without becoming a Member?",Html("Yes, Guardian Masterclasses are available to everyone from the Masterclasses website. However, only Partners can enjoy 20% off. In order to receive this discount Masterclasses must be booked through the Membership Masterclasses listings page.")),
      Item("How many Guardian Masterclasses places can I book for each course?",Html("You can book three tickets per transaction.")),
      Item("How will I know what Guardian Masterclasses events are coming up?",Html("Our Masterclasses listings page is updated regularly and when you join membership you will receive a weekly email newsletter containing information about upcoming courses.")),
      Item("Where do Guardian Masterclasses take place?",Html("Our Masterclasses are held in the meeting rooms and conference spaces of the Guardian's headquarters in King's Cross, London. View our maps to help you find your way to the Guardian offices, and find your way around inside. We also host Masterclasses in other venues in London and beyond.")),
      Item("What is the Guardian Masterclasses refund policy?",Html("You may cancel a Guardian Masterclass if your notification is received by the Masterclasses team a minimum of 14 days prior to the start of the Guardian Masterclass. Provided the Masterclasses team has received your notice of cancellation within this time frame (and acknowledged your notice), Masterclasses will refund any fees received from you less the deposit where applicable. Please ensure the Masterclasses team has acknowledged your notice of cancellation. We will not be able to refund you where we did not receive your notice 14 days before the start of the Guardian Masterclass (regardless of when you sent it). Our tickets are not transferable to other courses or dates. Find out how to contact the masterclass team here."))
    ))
  )
}
