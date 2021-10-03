#include <iostream>
#include <cmath>
#include <chrono>
#include <algorithm>
#include <vector>

using std::vector;
using std::max;

#define OLC_PGE_APPLICATION
#include "olcPixelGameEngine.h"

#define PI  ((float) 3.1415926534)

#define MAP_WIDTH  32
#define MAP_HEIGHT 32

#define COL_MAP_EMPTY    olc::DARK_BLUE
#define COL_MAP_SOLID    olc::BLUE
#define COL_PLAYER       olc::RED

#define MAX_NR_LEVELS 4

class myUltimateFPS : public olc::PixelGameEngine {
public:
    myUltimateFPS() {
        sAppName = "Ultimate first person shooter - Pixel - upgrade 2";  // this string is put in the title bar
    }

private:
    int nMapHeight = MAP_HEIGHT;  // dimensions of the map = playing field
    int nMapWidth  = MAP_WIDTH;
    std::string map;                  // map is created dynamically

    float fPlayerX = 25.0f;       // initial player position
    float fPlayerY = 11.0f;
    float fPlayerA = -PI;         // angle player is looking at (0.0f = east direction)
                                  // NOTE: due to flipping of y coordinate on screen,
                                  // angle is clockwise in + direction
    olc::Sprite *spriteWall[MAX_NR_LEVELS];     // different sprites for different levels
    olc::Sprite *spriteLamp;
    olc::Sprite *spriteFireBall;
    olc::Sprite *spriteMap[MAP_WIDTH][MAP_HEIGHT];

    float fFOV   = PI / 4.0;  // field of view = 45 degree's
    float fDepth = 25.0f;   // defines length of visibility
    float fSpeed =  5.0f;   // Walking speed

    float *fDepthBuffer = nullptr;
    int nViewLevel = 0;
    int nHorizon = ScreenHeight() / 2;

    struct sObject {
        float x;
        float y;
        float vx;
        float vy;
        olc::Sprite *sprite;
        bool bRemove;
        bool bIsInSight;
        bool bIsPickedUp;
    };

    vector<sObject> listObjects;

    bool bLightningOn;

// ==============================/ convenience functions /==============================

    bool outOfBounds( float x, float y, int min_x, int min_y, int max_x, int max_y ) {
        return (x < min_x || x >= max_x || y < min_y || y >= max_y);
    }

    bool outOfBounds( int x, int y, int min_x, int min_y, int max_x, int max_y ) {
        return (x < min_x || x >= max_x || y < min_y || y >= max_y);
    }

    // This function translates a variable a in a range between a_min and a_max into
    // a corresponding (i.e. proportional) result in the range b_min to b_max.
    int varyShade1( float a, float a_min, float a_max, int b_min, int b_max ) {
        return (int)(((a - a_min) / (a_max - a_min)) * (float)(b_max - b_min)) + b_min;
    }

    // This function translates a variable a in a range between a_min and a_max into
    // a corresponding (i.e. proportional) result in the range b_max to b_min.
    // Thus if a == a_min, then result == b_max !!
    int varyShade2( float a, float a_min, float a_max, int b_min, int b_max ) {
        return b_max - varyShade1( a, a_min, a_max, b_min, b_max );
    }
// ==============================/ Block characteristic functions /==============================

    bool IsEmptyBlock( wchar_t cBlock ) {
        return cBlock == '.';
    }

    bool IsLevelBlock( wchar_t cBlock, int nLevel ) {
        bool bReturn;
        switch (nLevel) {
            case 0: bReturn = (cBlock == '#'); break;
            case 1: bReturn = (cBlock == '*'); break;
            case 2: bReturn = (cBlock == '@'); break;
            case 3: bReturn = (cBlock == '$'); break;
        }
        return bReturn;
    }

    bool IsSolidBlock( wchar_t cBlock ) {
        bool bReturn = false;
        for (int i = 0; i < MAX_NR_LEVELS; i++)
            bReturn |= IsLevelBlock( cBlock, i );
        return bReturn;
    }

// ==============================/ stuff for Games28 /==============================

    // returns true if point of interest at (fPOI_x, fPOI_y) in map coordinates is in sight of the player.
    // This means that it is within [-fAngleMargin, +fAngleMargin] from the players looking angle.
    bool IsInSight( float fPOI_x, float fPOI_y, float fAngleMargin, float &fAngle2Player ) {

        auto ModuloTwoPi = [=]( float angle ) {        // utility lambda - returns angle modulo 2 * Pi
            float a = angle;
            while (a < 0)          a += 2.0f * PI;
            while (a >= 2.0f * PI) a -= 2.0f * PI;
            return a;
        };

        // calculate angle from POI to player
        float fTmp_x = fPOI_x - fPlayerX;
        float fTmp_y = fPOI_y - fPlayerY;

        // atan returns in <-pi, pi], clamp so that result is in [0, 2* PI>
        fAngle2Player = ModuloTwoPi( atan2( fTmp_y, fTmp_x ));

        // convert players looking angle fPlayerA to an angle that is aligned with atan() result angle
        // fPlayerA == 0 for EAST, whereas atan returns 0 for WEST so compensate for it
        float fAlignedA = fPlayerA - 1.0f * PI;

        // calculate whether POI is in sight of player - in sight occurs when one angle shifted by Pi
        // is within a margin of the other angle
        return abs( ModuloTwoPi( fAlignedA + PI ) - fAngle2Player) < fAngleMargin;
    }

    // Assumption: the player is available as a global or as a class variable.
    // Object is the object to be teleported, the two float parameters are the angle of the player before and after rotation.
    // This means that the new rotation angle of the player is calculated *before calling this function*, and is
    // passed as a parameter.
    void telekinesis_on_rotation( sObject &object, float angle_before_rotation, float angle_after_rotation ) {

        float difference_x = object.x - fPlayerX;
        float difference_y = object.y - fPlayerY;

        float distance = sqrtf( difference_x * difference_x + difference_y * difference_y );
        float angle_player_to_object = atan2f( difference_y, difference_x );

        float angle_difference = angle_after_rotation - angle_before_rotation;
        object.x += distance * (cosf( angle_player_to_object + angle_difference ) - cosf( angle_player_to_object ));
        object.y += distance * (sinf( angle_player_to_object + angle_difference ) - sinf( angle_player_to_object ));
    }

    // Assumption: the player is available as a global or as a class variable
    // The four float parameters are the position of the player before and after strafing. This implies that the
    // new position of the player is calculated *before calling this function*, and is passed as a parameter.
    void telekinesis_on_strafing( sObject &object, float plyr_x_before, float plyr_y_before, float plyr_x_after, float plyr_y_after ) {

        object.x += plyr_x_after - plyr_x_before;
        object.y += plyr_y_after - plyr_y_before;
    }

    // Draws a randomized lightning pattern between firstPoint and lastPoint, by calculating a
    // randomized mid point to create two subsegments, and recursively calling itself on the two subsegments
    // The recursion depth is controlled by parameter depth - if depth == 0 then straight line is result
    // NOTE: the function is O(2^n) w.r.t. the recursion depth, so don't dig too deep :)
    void DrawRandomLightning( int x1, int y1, int x2, int y2, int depth ) {

        // lambda returns a random float nr in range [-1, +1]
        auto RandAroundZero = [=]() {
            return ((float)rand() / (float)RAND_MAX) * 2.0f - 1.0f;
        };

        if (depth == 0) {
            // render line segment of lightning
            DrawLine( x1, y1, x2, y2, olc::YELLOW );
        } else if (depth > 0) {

            // determine some random point some where in the middle
            int nDeltaX = x2 - x1;
            int nDeltaY = y2 - y1;

            float fPercentage =  0.30f;   // percentage of delta X resp. Y to be use for randomizaton
            float fMinimum    = 10.00f;   // if delta X resp Y is below this minimum, then use this minimum

            float fMarginX = max( fMinimum, fPercentage * (float)nDeltaX );
            float fMarginY = max( fMinimum, fPercentage * (float)nDeltaY );

            int x_mid = x1 + nDeltaX / 2 + (int)(RandAroundZero() * fMarginX);
            int y_mid = y1 + nDeltaY / 2 + (int)(RandAroundZero() * fMarginY);

            // recursive call for subsegments
            DrawRandomLightning( x1   , y1   , x_mid, y_mid, depth - 1 );
            DrawRandomLightning( x_mid, y_mid, x2   , y2   , depth - 1 );
        }
    }

// ==============================/ OnUserCreate() / OnUserUpdate() /==============================

    virtual bool OnUserCreate() {
        // create the map - inspired bij wolvenstein 3d game
        map += "################################";      // 32 x 32 version
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#......@##*.......*##@.........#";
        map += "#......#.............#.........#";
        map += "#......#.............#.........#";
        map += "#......*.............*.........#";
        map += "#......#.............#.........#";
        map += "#......#.....###.....#.........#";
        map += "#......#.....*$*.....#.........#";
        map += "#......*.............*.........#";
        map += "#......#.............#.........#";
        map += "#......#.............#.........#";
        map += "#......@###*.....*###@.........#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#..............................#";
        map += "#........#...*...@...$.........#";
        map += "#..............................#";
        map += "################################";

        spriteWall[0]  = new olc::Sprite( "wall_brd.png" );
        spriteWall[1]  = new olc::Sprite( "wall_brd.png" );
        spriteWall[2]  = new olc::Sprite( "unknown.png" );
        spriteWall[3]  = new olc::Sprite( "unknown.png" );
        spriteLamp     = new olc::Sprite( "star.png" );
        spriteFireBall = new olc::Sprite( "star.png" );

        fDepthBuffer = new float[ ScreenWidth() ];   // depth buffer works per column

        for (int y = 0; y < nMapHeight; y++)                // set all sprite pointers in spriteMap to default sprite for walls
            for (int x = 0; x < nMapWidth; x++) {
                if      (IsEmptyBlock( map[ y * nMapWidth + x ]    )) spriteMap[x][y] = nullptr;
                else if (IsLevelBlock( map[ y * nMapWidth + x ], 0 )) spriteMap[x][y] = spriteWall[0];
                else if (IsLevelBlock( map[ y * nMapWidth + x ], 1 )) spriteMap[x][y] = spriteWall[1];
                else if (IsLevelBlock( map[ y * nMapWidth + x ], 2 )) spriteMap[x][y] = spriteWall[2];
                else if (IsLevelBlock( map[ y * nMapWidth + x ], 3 )) spriteMap[x][y] = spriteWall[3];
            }

        sObject theLamp = { 8.5f, 8.5f, 0.0f, 0.0f, spriteLamp, false, false, false };
        listObjects.push_back( theLamp );

        return true;
    }

    // this function looks for a block in the map as denoted by cBlock, and returns true if found. Returns false if not found
    // within class variable value fDepth.
    // fRayA is the angle of the ray from the player into the world. If found, the function returns values for the distance to
    // the block, the coordinates of the block in map space and the x-sample to texture the block colour with a sprite.
    bool FindDistanceToBlock( int nLevel, float fRayA, float &fDistance, int &nBlockX, int &nBlockY, float &fXsample ) {

        float fEyeX = cosf( fRayA );  // Unit vector for ray in player space
        float fEyeY = sinf( fRayA );

        fXsample        = 0.0f;
        fDistance       = 0.0f;;
        float fStepSize = 0.01f;      // increment size for raycasting - decrease for higher resolution
        bool bHitWall   = false;

        while (!bHitWall && fDistance < fDepth) {

            fDistance += fStepSize;  // increase distance until either found or max depth is reached
            nBlockX = (int)(fPlayerX + fEyeX * fDistance );
            nBlockY = (int)(fPlayerY + fEyeY * fDistance );

            if (outOfBounds( nBlockX, nBlockY, 0, 0, nMapWidth, nMapHeight)) {
                fDistance = fDepth;  // if ray is out of bounds of map, set distance to maximum depth
            } else {
                // Ray is in bounds so test to see if the ray cell is a block of interest
                if (IsLevelBlock( map[nBlockY * nMapWidth + nBlockX], nLevel)) {
                    bHitWall = true;

                    // Determine where ray has hit wall. Break Block boundary
                    // into 4 line segments
                    float fBlockMidX = (float)nBlockX + 0.5f;   // location of middle of the cell that was hit
                    float fBlockMidY = (float)nBlockY + 0.5f;

                    float fTestPointX = fPlayerX + fEyeX * fDistance;  // location of the point of collision
                    float fTestPointY = fPlayerY + fEyeY * fDistance;

                    float fTestAngle = atan2f((fTestPointY - fBlockMidY), (fTestPointX - fBlockMidX));
                    // evaluate the four possible outcomes of atan2f()
                    if (-0.75f * PI <= fTestAngle && fTestAngle <  -0.25f * PI) fXsample = fTestPointX - (float)nBlockX;
                    if (-0.25f * PI <= fTestAngle && fTestAngle <   0.25f * PI) fXsample = fTestPointY - (float)nBlockY;
                    if ( 0.25f * PI <= fTestAngle && fTestAngle <   0.75f * PI) fXsample = fTestPointX - (float)nBlockX;
                    if (-0.75f * PI >  fTestAngle || fTestAngle >=  0.75f * PI) fXsample = fTestPointY - (float)nBlockY;
                }
            }
        }
        return bHitWall;
    }

    virtual bool OnUserUpdate( float fElapsedTime ) {

        // check if object is "picked up" for telekinesis
        float fObj2PlyA;
        for (auto &object : listObjects) {
            // only pick up stationary objects
            if (object.vx == 0.0f && object.vy == 0.0f) {
                object.bIsInSight = IsInSight( object.x, object.y, 3.0f * (PI / 180.0f), fObj2PlyA );
                if (object.bIsInSight && GetKey( olc::Key::P ).bHeld)
                    object.bIsPickedUp = true;
                else
                    object.bIsPickedUp = false;
            }
        }

        // ========== Movement controls ==========q

        float fCacheA = fPlayerA;    // cache player angle before rotation

        // Handle CCW rotation (uses A)
        if (GetKey( olc::Key::A ).bHeld) {
            fPlayerA -= fSpeed * 0.2f * fElapsedTime;
            for (auto &object : listObjects)
                if (object.bIsPickedUp)
                    telekinesis_on_rotation( object, fCacheA, fPlayerA );
        }

        // Handle CW rotation (uses D)
        if (GetKey( olc::Key::D ).bHeld) {
            fPlayerA += fSpeed * 0.2f * fElapsedTime;
            for (auto &object : listObjects)
                if (object.bIsPickedUp)
                    telekinesis_on_rotation( object, fCacheA, fPlayerA );
        }

// NOTE: the collision detection checks below should be complemented with out of bounds checks on the fPlayerX/Y coords
// otherwise the player can stray off the map if there are no blocks to stop him

        float myTmpSin = sinf( fPlayerA ) * fSpeed * fElapsedTime;
        float myTmpCos = cosf( fPlayerA ) * fSpeed * fElapsedTime;
        float fCacheX = fPlayerX;
        float fCacheY = fPlayerY;

        // Handle forward movement & collision (uses W)
        if (GetKey( olc::Key::W ).bHeld) {

            fPlayerX += myTmpCos;
            fPlayerY += myTmpSin;

            if (outOfBounds( fPlayerX, fPlayerY, 0, 0, nMapWidth, nMapHeight ) ||     // out of bounds check
                IsSolidBlock( map[(int)fPlayerY * nMapWidth + (int)fPlayerX] )) {     // collision detection

                fPlayerX -= myTmpCos;
                fPlayerY -= myTmpSin;
            } else {
                for (auto &object : listObjects)
                    if (object.bIsPickedUp)
                        telekinesis_on_strafing( object, fCacheX, fCacheY, fPlayerX, fPlayerY );
            }
        }
        // Handle backwards movement & collision (uses S)
        if (GetKey( olc::Key::S ).bHeld) {
            fPlayerX -= myTmpCos;
            fPlayerY -= myTmpSin;

            if (outOfBounds( fPlayerX, fPlayerY, 0, 0, nMapWidth, nMapHeight ) ||     // out of bounds check
                IsSolidBlock( map[(int)fPlayerY * nMapWidth + (int)fPlayerX] )) {     // collision detection

                fPlayerX += myTmpCos;
                fPlayerY += myTmpSin;
            } else {
                for (auto &object : listObjects)
                    if (object.bIsPickedUp)
                        telekinesis_on_strafing( object, fCacheX, fCacheY, fPlayerX, fPlayerY );
            }
        }
        // Handle strafe (move sideways) left movement & collision (uses Q)
        if (GetKey( olc::Key::Q ).bHeld) {
            fPlayerX += myTmpSin;
            fPlayerY -= myTmpCos;

            if (outOfBounds( fPlayerX, fPlayerY, 0, 0, nMapWidth, nMapHeight ) ||     // out of bounds check
                IsSolidBlock( map[(int)fPlayerY * nMapWidth + (int)fPlayerX] )) {     // collision detection

                fPlayerX -= myTmpSin;
                fPlayerY += myTmpCos;
            } else {
                for (auto &object : listObjects)
                    if (object.bIsPickedUp)
                        telekinesis_on_strafing( object, fCacheX, fCacheY, fPlayerX, fPlayerY );
            }
        }
        // Handle strafe (move sideways) right movement & collision (uses E)
        if (GetKey( olc::Key::E ).bHeld) {  // Move sideways to left
            fPlayerX -= myTmpSin;
            fPlayerY += myTmpCos;

            if (outOfBounds( fPlayerX, fPlayerY, 0, 0, nMapWidth, nMapHeight ) ||     // out of bounds check
                IsSolidBlock( map[(int)fPlayerY * nMapWidth + (int)fPlayerX] )) {     // collision detection

                fPlayerX += myTmpSin;
                fPlayerY -= myTmpCos;
            } else {
                for (auto &object : listObjects)
                    if (object.bIsPickedUp)
                        telekinesis_on_strafing( object, fCacheX, fCacheY, fPlayerX, fPlayerY );
            }
        }
        // fire bullets
        if (GetKey( olc::Key::SPACE ).bHeld) {
            sObject o;
            o.x = fPlayerX;
            o.y = fPlayerY;
            float fNoise = (((float)rand() / (float)RAND_MAX) - 0.5f) * 0.05f;
            o.vx = cosf( fPlayerA + fNoise ) * 20.0f;   // isn't this the wrong way around?
            o.vy = sinf( fPlayerA + fNoise ) * 20.0f;

            o.sprite      = spriteFireBall;
            o.bIsInSight  = false;
            o.bIsPickedUp = false;
            o.bRemove     = false;
            listObjects.push_back( o );
        }
        // Check if lightning is activated by playerqqpddadswwaaa
        bLightningOn = GetKey( olc::Key::L ).bHeld;

        if (GetKey( olc::Key::NP_ADD ).bReleased) {
            nViewLevel += 1;
            if (nViewLevel >= MAX_NR_LEVELS)
                nViewLevel = MAX_NR_LEVELS - 1;
        }
        if (GetKey( olc::Key::NP_SUB ).bReleased) {
            nViewLevel -= 1;
            if (nViewLevel < 0)
                nViewLevel = 0;
        }

// Rendering algorithm: blocks, viewfield, ScreenWidth() columns from the screen. Sort of ray tracing per column.

        for (int x = 0; x < ScreenWidth(); x++ ) {   // iterate over all pixels in the screen

            // For each column, calculate the projected ray angle into world space
            float fRayAngle = (fPlayerA - fFOV / 2.0f) + ((float)x / (float)ScreenWidth()) * fFOV;

            // these arrays are used to represent the different levels, where level 0 = base level, regular floor level
            int   nTestX[MAX_NR_LEVELS],
                  nTestY[MAX_NR_LEVELS];
            float fDistToWall[MAX_NR_LEVELS] = { 0.0f };
            bool  bHitWall[MAX_NR_LEVELS] = { false };
            float fSampleX[MAX_NR_LEVELS] = { 0.0f };

            // find distance to wall for this ray on both levels
            for (int i = 0; i < MAX_NR_LEVELS; i++)
                bHitWall[i] = FindDistanceToBlock( i, fRayAngle, fDistToWall[i], nTestX[i], nTestY[i], fSampleX[i] );
            // if there are no level n-1 blocks before the level n block in this ray, the variables for level n-1 must still be set
            // so set the level n-1 variables equal to the level n variables
            // NOTE: this is a consequence of my choice to represent levels in a 2d map.
            for (int i = MAX_NR_LEVELS - 2; i >= 0; i--) {
                if (fDistToWall[i] > fDistToWall[i + 1]) {
                    bHitWall[i]    = bHitWall[i + 1];    // don't forget to set this, calculation of nCeiling[] and nFloor[] depends on it
                    fDistToWall[i] = fDistToWall[i + 1];
                    nTestX[i]      = nTestX[i + 1];
                    nTestY[i]      = nTestY[i + 1];
                    fSampleX[i]    = fSampleX[i + 1];
                }
            }

// NOTE w.r.t. values of nCeiling and nFloor --> see document "Notes on nCeiling and nFloor.docx"

            int nCeiling[MAX_NR_LEVELS];
            int nFloor[  MAX_NR_LEVELS];
            // Calculate values for ceiling and floor for base level
            nCeiling[nViewLevel] = (float)(ScreenHeight() / 2.0) - (float)ScreenHeight() / ((float)fDistToWall[nViewLevel]);
            nFloor[  nViewLevel] = ScreenHeight() - nCeiling[nViewLevel];
            nHorizon = (ScreenHeight() / 2) + nViewLevel * (nFloor[nViewLevel] - nCeiling[nViewLevel]) / 2;

            // calculate levels below base level
            for (int i = 0; i < nViewLevel; i++) {
                if (bHitWall[i]) {
                    // if there is a block on the current level:
                    // 1. calculate ceiling, floor and height of this cell at the horizon (= base) level
                    int nBaseCeiling = (float)(ScreenHeight() / 2.0) - (float)ScreenHeight() / ((float)fDistToWall[i]);
                    int nBaseFloor   = ScreenHeight() - nBaseCeiling;
                    int nBaseHeight  = (nBaseFloor - nBaseCeiling);
                    // 2. use that height to calculate floor and ceiling values of block at current level
                    nCeiling[i] = nBaseCeiling - i * nBaseHeight;
                    nFloor[i]   = nBaseFloor   - i * nBaseHeight;
//                    // make sure ceiling ranges do not overlap - correct if ceiling is too low
//                    // otherwise farther away blocks on a higher level are rendered on top of a closer block on a lower level,
//                    // in cases where the farther away block is completely covered by the closer block
//                    if (nCeiling[i] > nCeiling[i - 1])
//                        nCeiling[i] = nCeiling[i - 1];
                }
            }
            for (int i = 0; i < nViewLevel; i++)
                if (!bHitWall[i]) {
                    // if there is no block on the current level, set it's floor & ceiling levels equal to the lower level ceiling
                    // to prevent the block from being rendered
                    nCeiling[i] = nCeiling[i - 1];
                    nFloor[  i] = nCeiling[i - 1];
                }

            // calculate levels above base level
            for (int i = nViewLevel + 1; i < MAX_NR_LEVELS; i++) {
                if (bHitWall[i]) {
                    // if there is a block on the current level:
                    // 1. calculate ceiling, floor and height of this cell at the lowest (= base) level
                    int nBaseCeiling = (float)(ScreenHeight() / 2.0) - (float)ScreenHeight() / ((float)fDistToWall[i]);
                    int nBaseFloor   = ScreenHeight() - nBaseCeiling;
                    int nBaseHeight  = (nBaseFloor - nBaseCeiling);
                    // 2. use that height to calculate floor and ceiling values of block at current level
                    nCeiling[i] = nBaseCeiling - i * nBaseHeight;
                    nFloor[i]   = nBaseFloor   - i * nBaseHeight;
                    // make sure ceiling ranges do not overlap - correct if ceiling is too low
                    // otherwise farther away blocks on a higher level are rendered on top of a closer block on a lower level,
                    // in cases where the farther away block is completely covered by the closer block
                    if (nCeiling[i] > nCeiling[i - 1])
                        nCeiling[i] = nCeiling[i - 1];
                }
            }
            for (int i = nViewLevel + 1; i < MAX_NR_LEVELS; i++)
                if (!bHitWall[i]) {
                    // if there is no block on the current level, set it's floor & ceiling levels equal to the lower level ceiling
                    // to prevent the block from being rendered
                    nCeiling[i] = nCeiling[i - 1];
                    nFloor[  i] = nCeiling[i - 1];
                }

            // Update depth buffer
            fDepthBuffer[x] = fDistToWall[0];    // temporary fix - depth buffer will currently only work on floor level

            for (int y = 0; y < ScreenHeight(); y++) {    // iterate this column vertically, pixel by pixel

                if (y <= nCeiling[MAX_NR_LEVELS - 1]) {            // ceiling
                                        // vary the blue component of rgb-pixel -- CAUTION: hardcoded color value !!
//                    float bValue = (float)y / ((float) ScreenHeight() / 2.0f);
                    float bValue = (float)y / (float)nHorizon;
                    Draw( x, y, olc::Pixel( 0, 0, varyShade2( bValue, -1.0f, 0.0f, 64, 255 )));
                } else if (y > nFloor[0]) {                        // Floor

                                        // vary the blue component of rgb-pixel -- CAUTION: hardcoded color value !!

//                    float b = 1.0f - (((float) y - ScreenHeight() / 2.0f) / ((float) ScreenHeight() / 2.0f));
                    float b = 1.0f - (((float) y - (float)nHorizon) / (float)nHorizon);
                    Draw( x, y, olc::Pixel( 0, varyShade2( b, -1.0f, 0.0f, 64, 255 ), 0));

                } else {
                    // since y is not in the ceiling range nor in the floor range, it must be in the wall / levels range
                    // look for current level - the search is a bit different for blocs on or below resp. above horizon
                    int nCurLevel = -1;
                    for (int i = 0; nCurLevel == -1 && i < MAX_NR_LEVELS; i++) {
                        if (i <= nViewLevel) {
                            if (y > nCeiling[i] && y <= nFloor[i])
                                nCurLevel = i;
                        } else {
                            // note that i > nViewLevel, so that i - 1 is safe indexing...
                            if (y > nCeiling[i] && y <= nCeiling[i - 1])
                                nCurLevel = i;
                        }
                    }

                    if (nCurLevel == -1) {                    // if current level was not found render it as if it were floor
                            Draw( x, y, olc::YELLOW );        // TEMPORARY
                    } else {
                        if (fDistToWall[nCurLevel] < fDepth) {            // Draw only if visible
                            olc::Sprite *tmpSpritePtr = spriteMap[nTestX[nCurLevel]][nTestY[nCurLevel]];  // get a pointer to the sprite associated with this cell & level

                            float fSampleY = ((float)y - (float)nCeiling[nCurLevel]) / ((float)nFloor[nCurLevel] - (float)nCeiling[nCurLevel]);
                            Draw( x, y, tmpSpritePtr->Sample( fSampleX[nCurLevel], fSampleY ));
                        } else {
                            Draw( x, y, olc::VERY_DARK_GREY );       // Wall is too far to see
                        }
                    }
                }
            }
        }

        // update & draw objects
        for (auto &object : listObjects) {

            // update object physics
            object.x += object.vx * fElapsedTime;
            object.y += object.vy * fElapsedTime;

            // check if object is inside wall - set flag for removal
            // only for objects that are not picked up to prevent killing objects by telekinesis
            if (!object.bIsPickedUp && IsSolidBlock( map[(int)object.x * nMapWidth + (int)object.y] ))
                object.bRemove = true;

            // can object be seen? Is it within reasonable distance?
            float fVecX = object.x - fPlayerX;
            float fVecY = object.y - fPlayerY;
            float fDistanceFromPlayer = sqrtf( fVecX * fVecX + fVecY * fVecY );

            // calculate angle between lamp and players feet, and players looking angle
            // to determine if the lamp is in players field of view
            float fEyeX = cosf( fPlayerA );
            float fEyeY = sinf( fPlayerA );
            float fObjectAngle = atan2f( fVecY, fVecX ) - atan2f( fEyeY, fEyeX );
            if (fObjectAngle < -PI) fObjectAngle += 2.0f * PI;
            if (fObjectAngle >  PI) fObjectAngle -= 2.0f * PI;
            // compare angle from player to object with field of view of player
            bool bInPlayerFOV = fabs( fObjectAngle ) < fFOV / 2.0f;

            // only draw if within FOV, distance not too large, and not too small
            if (bInPlayerFOV && fDistanceFromPlayer >= 0.5f && fDistanceFromPlayer < fDepth) {

                float fObjectCeiling = (float)(ScreenHeight() / 2.0f) - ScreenHeight() / ((float)fDistanceFromPlayer);
                float fObjectFloor   = ScreenHeight() - fObjectCeiling;
                float fObjectHeight = fObjectFloor - fObjectCeiling;
                float fObjectAspectRatio = (float)object.sprite->height / (float)object.sprite->width;
                float fObjectWidth = fObjectHeight / fObjectAspectRatio;

                float fMiddleOfObject = (0.5f * (fObjectAngle / (fFOV / 2.0f)) + 0.5f) * (float)ScreenWidth();

                for (float lx = 0; lx < fObjectWidth; lx++) {
                    for (float ly = 0; ly < fObjectHeight; ly++) {
                        float fSampleX = lx / fObjectWidth;
                        float fSampleY = ly / fObjectHeight;
//                        wchar_t c = object.sprite->SampleGlyph( fSampleX, fSampleY );
                        int nObjectColumn = (int)(fMiddleOfObject + lx - (fObjectWidth / 2.0f));
                        if (nObjectColumn >= 0 && nObjectColumn < ScreenWidth()) {
                            if ( /* c != L' ' && */ fDepthBuffer[nObjectColumn] >= fDistanceFromPlayer) {
                                Draw( nObjectColumn, fObjectCeiling + ly, object.sprite->Sample( fSampleX, fSampleY ));
                                fDepthBuffer[nObjectColumn] = fDistanceFromPlayer;
                            }
                        }
                    }
                }
                if (object.bIsInSight)
                    DrawRect( fMiddleOfObject - fObjectWidth / 2.0f, fObjectCeiling,
                              fObjectWidth, fObjectHeight, object.bIsPickedUp ? olc::RED : olc::YELLOW );
            }
        }

        if (bLightningOn) {
            int x1 = ScreenWidth() * 1 / 4;
            int x2 = ScreenWidth() * 3 / 4;
            DrawRandomLightning( x1, ScreenHeight() * 9 / 10, ScreenWidth() / 2, ScreenHeight() * 5 / 10, 5 );
            DrawRandomLightning( x2, ScreenHeight() * 9 / 10, ScreenWidth() / 2, ScreenHeight() * 5 / 10, 5 );
        }

//        DrawLine( 0, nHorizon, ScreenWidth(), nHorizon, olc::MAGENTA, 0xFF00FF00 );

        // remove dead objects from object list using stl remove_if() call
        std::remove_if( listObjects.begin(), listObjects.end(), [](sObject &o) { return o.bRemove; });

        // Display Map - offset from border of screen
        for (int nx = 0; nx < nMapWidth; nx++)
            for (int ny = 0; ny < nMapHeight; ny++) {
                if      (IsEmptyBlock( map[ ny * nMapWidth + nx ] )) Draw( nx + 1, ny + 1,  COL_MAP_EMPTY );
                else if (IsSolidBlock( map[ ny * nMapWidth + nx ] )) Draw( nx + 1, ny + 1,  COL_MAP_SOLID );
            }
        Draw( 1 + (int) fPlayerX, 1 + (int) fPlayerY, COL_PLAYER );   // show where player is in the map

        return true;
     }
};

// Vary the pixel size between 2 and 8. The High def constants are the max screen width / height for
// pixel size == 1 (but that's not working)

// pixel size <  4 --> map is hardly readable
// pixel size >= 8 --> readable text

#define PIXEL_SIZE      2

#define HIGH_DEF_X   1280
#define HIGH_DEF_Y    800

int main()
{
    myUltimateFPS game;

    if (game.Construct( HIGH_DEF_X / PIXEL_SIZE, HIGH_DEF_Y / PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE ))
        game.Start();

    return 0;
}
