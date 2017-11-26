package co.edu.ud.scrumgraph.data.enums;

import org.neo4j.graphdb.RelationshipType;

public  enum ERelTypes implements RelationshipType
{
    BELONGS_TO , WORKING_ON , WORKS_ON, PERFORMS , IS_COMPOSED, IS_ASSIGNED_TO ;
	
}