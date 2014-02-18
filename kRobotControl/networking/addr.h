// -*-c++-*-

/***************************************************************************
                          addr.hpp  - A network address class
                             -------------------
    begin                : 07-JAN-2003
    copyright            : (C) 2003 by The RoboCup Soccer Server
                           Maintenance Group.
    email                : sserver-admin@lists.sourceforge.net
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU LGPL as published by the Free Software  *
 *   Foundation; either version 2 of the License, or (at your option) any  *
 *   later version.                                                        *
 *                                                                         *
 ***************************************************************************/

#ifndef RCSS_NET_ADDR_HPP
#define RCSS_NET_ADDR_HPP

struct sockaddr_in;

#include <string>
//#include <boost/shared_ptr.hpp>
#include <stdint.h>

namespace net{
	
        class AddrImpl;

        class Addr
        {
        public:
            typedef uint16_t PortType;
            typedef uint32_t HostType;
            typedef struct sockaddr_in AddrType;

            static const HostType BROADCAST;
            static const HostType ANY;

            Addr();
            Addr( const AddrType& addr );
            Addr( PortType port );
            Addr( PortType port, HostType host );
            Addr( PortType port, const std::string& host );
            ~Addr();

            const AddrType& getAddr() const;
            PortType getPort() const;
            void setPort(PortType port);
            HostType getHost() const;
            std::string getHostStr() const;

            bool operator == ( const Addr& addr ) const;
            bool operator < (const Addr& addr ) const;
            Addr& operator = ( const Addr& other );

        private:
            AddrImpl* m_impl;
        };

        std::ostream&
        operator<<( std::ostream& o, const Addr& addr );

} // namespace net

#endif

